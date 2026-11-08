package com.example.ime

import android.content.Intent
import android.content.ClipboardManager
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService.Insets
import android.os.Build
import android.content.ClipDescription
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.SuggestionSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.MBoardPreferences
import com.example.data.SuggestionEngine
import com.example.data.TextIssue
import com.example.ui.MBoardKeyboardView

class MBoardInputMethodService : LifecycleInputMethodService() {

    private lateinit var preferences: MBoardPreferences
    private lateinit var suggestionEngine: SuggestionEngine
    private lateinit var clipboardManager: ClipboardManager

    private val currentTextState = mutableStateOf("")
    private val clipboardSuggestionState = mutableStateOf<String?>(null)
    private val enterLabelState = mutableStateOf("↵")
    private var currentKeyboardBounds: Rect? = null
    private var keyboardComposeView: ComposeView? = null
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        updateClipboardSuggestion()
    }
    private var clipboardListenerRegistered = false

    override fun onCreate() {
        super.onCreate()
        preferences = MBoardPreferences(this)
        suggestionEngine = SuggestionEngine(preferences, this)
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Prevent Android from creating a full-screen extracted text black container
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        enterLabelState.value = enterLabelFor(info)
        updateInputViewHeight(preferences.isFloating)
        updateWindowLayoutForFloating()
        updateCurrentText()
        registerClipboardListener()
        updateClipboardSuggestion()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        unregisterClipboardListener()
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        unregisterClipboardListener()
        super.onDestroy()
    }

    private fun registerClipboardListener() {
        if (!clipboardListenerRegistered) {
            clipboardManager.addPrimaryClipChangedListener(clipboardListener)
            clipboardListenerRegistered = true
        }
    }

    private fun unregisterClipboardListener() {
        if (clipboardListenerRegistered) {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener)
            clipboardListenerRegistered = false
        }
    }

    private fun updateClipboardSuggestion() {
        val description = clipboardManager.primaryClipDescription
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            description?.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE) == true
        ) {
            clipboardSuggestionState.value = null
            return
        }

        val copiedText = clipboardManager.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.text
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        clipboardSuggestionState.value = copiedText
        if (copiedText != null) {
            preferences.addClipboardItem(copiedText)
        }
    }

    override fun onConfigureWindow(win: Window, isFullscreen: Boolean, isCandidatesOnly: Boolean) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly)
        // Ensure transparent backdrop so no black container is created over the host application
        win.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        win.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        updateWindowLayoutForFloating(win)
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val decorView = window?.window?.decorView
        val decorHeight = decorView?.height ?: resources.displayMetrics.heightPixels
        val decorWidth = decorView?.width ?: resources.displayMetrics.widthPixels

        if (::preferences.isInitialized && preferences.isFloating) {
            // Floating overlay mode: don't squish host app
            outInsets.contentTopInsets = decorHeight
            outInsets.visibleTopInsets = decorHeight

            // Deliver touch events only around the floating keyboard card.
            // Generous margin ensures moving towards the bottom or edges never aborts touch events!
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION
            val bounds = currentKeyboardBounds
            val density = resources.displayMetrics.density
            val marginX = (32 * density).toInt()
            val marginY = (48 * density).toInt()

            if (bounds != null && !bounds.isEmpty) {
                val left = (bounds.left - marginX).coerceAtLeast(0)
                val top = (bounds.top - marginY).coerceAtLeast(0)
                val right = (bounds.right + marginX).coerceAtMost(decorWidth)
                val bottom = (bounds.bottom + marginY).coerceAtMost(decorHeight)
                outInsets.touchableRegion.set(left, top, right, bottom)
            } else {
                val kbW = (decorWidth * preferences.floatingScale).toInt()
                val kbH = (270 * density).toInt()
                val left = ((decorWidth - kbW) / 2 + preferences.floatingPositionX.toInt()).coerceIn(0, (decorWidth - kbW).coerceAtLeast(0))
                val top = (decorHeight - kbH + preferences.floatingPositionY.toInt()).coerceIn(0, (decorHeight - kbH).coerceAtLeast(0))
                outInsets.touchableRegion.set(
                    (left - marginX).coerceAtLeast(0),
                    (top - marginY).coerceAtLeast(0),
                    (left + kbW + marginX).coerceAtMost(decorWidth),
                    (top + kbH + marginY).coerceAtMost(decorHeight)
                )
            }
        }
    }

    private fun commitTypedText(text: String) {
        val inputConnection = currentInputConnection ?: return
        val beforeCursor = inputConnection.getTextBeforeCursor(512, 0)?.toString().orEmpty()
        val isSeparator = text.isNotEmpty() && text.all {
            it.isWhitespace() || it in ".,!?;:"
        }

        if (!isSeparator || !supportsTypingAssistance(currentInputEditorInfo)) {
            inputConnection.commitText(text, 1)
            updateCurrentText()
            return
        }

        val currentWord = beforeCursor.takeLastWhile {
            it.isLetter() || it == '\'' || it == '’' || it == '-'
        }
        if (currentWord.isEmpty()) {
            inputConnection.commitText(text, 1)
            updateCurrentText()
            return
        }

        val spellingSuggestion = suggestionEngine.getSpellingSuggestion(currentWord)
        val previousWord = beforeCursor.dropLast(currentWord.length).trimEnd().takeLastWhile {
            it.isLetter() || it == '\'' || it == '’' || it == '-'
        }.takeIf { it.isNotBlank() }
        if (preferences.autoCorrection && spellingSuggestion != null) {
            val correctedWord = if (currentWord.first().isUpperCase()) {
                spellingSuggestion.replaceFirstChar { it.uppercase() }
            } else {
                spellingSuggestion
            }
            inputConnection.beginBatchEdit()
            try {
                inputConnection.deleteSurroundingText(currentWord.length, 0)
                inputConnection.commitText(correctedWord + text, 1)
                suggestionEngine.learnWord(correctedWord, previousWord)
            } finally {
                inputConnection.endBatchEdit()
            }
            updateCurrentText()
            return
        }

        val grammarIssue = if (preferences.grammarCheckingEnabled) {
            suggestionEngine.findGrammarIssue(beforeCursor)
        } else {
            null
        }
        val spellingIssue = if (preferences.spellingUnderlineEnabled && spellingSuggestion != null) {
            val start = beforeCursor.length - currentWord.length
            TextIssue(start, beforeCursor.length, spellingSuggestion)
        } else {
            null
        }
        val issue = grammarIssue ?: spellingIssue

        if (issue == null || issue.end != beforeCursor.length || issue.start !in 0 until issue.end) {
            inputConnection.commitText(text, 1)
            if (spellingSuggestion == null) {
                suggestionEngine.learnWord(currentWord, previousWord)
            }
            updateCurrentText()
            return
        }

        val issueText = beforeCursor.substring(issue.start, issue.end)
        if (spellingSuggestion == null) {
            suggestionEngine.learnWord(currentWord, previousWord)
        }
        val annotation = SpannableString(issueText)
        val suggestionFlags = if (
            grammarIssue != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        ) {
            SuggestionSpan.FLAG_GRAMMAR_ERROR
        } else {
            SuggestionSpan.FLAG_EASY_CORRECT
        }
        annotation.setSpan(
            SuggestionSpan(this, arrayOf(issue.suggestion), suggestionFlags),
            0,
            annotation.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        inputConnection.beginBatchEdit()
        try {
            inputConnection.deleteSurroundingText(issueText.length, 0)
            inputConnection.commitText(annotation, 1)
            inputConnection.commitText(text, 1)
        } finally {
            inputConnection.endBatchEdit()
        }
        updateCurrentText()
    }

    private fun supportsTypingAssistance(editorInfo: EditorInfo?): Boolean {
        val inputType = editorInfo?.inputType ?: return false
        if (inputType and InputType.TYPE_MASK_CLASS != InputType.TYPE_CLASS_TEXT) return false
        if (inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0) return false

        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return variation != InputType.TYPE_TEXT_VARIATION_PASSWORD &&
            variation != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD &&
            variation != InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD &&
            variation != InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS &&
            variation != InputType.TYPE_TEXT_VARIATION_URI &&
            variation != InputType.TYPE_TEXT_VARIATION_FILTER
    }

    private fun updateWindowLayoutForFloating(win: Window? = window?.window) {
        val targetWindow = win ?: window?.window ?: return
        // Keep window completely transparent in all modes
        targetWindow.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        targetWindow.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        targetWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        if (::preferences.isInitialized && preferences.isFloating) {
            // Expand Window to full screen MATCH_PARENT so the floating keyboard can move anywhere on screen
            targetWindow.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            targetWindow.setGravity(Gravity.FILL)
            val lp = targetWindow.attributes
            lp.gravity = Gravity.FILL
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            lp.flags = lp.flags or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            targetWindow.attributes = lp

            // Ensure containers don't clamp the keyboard and remain transparent
            targetWindow.decorView.post {
                try {
                    val inputArea = targetWindow.findViewById<View>(android.R.id.inputArea)
                    if (inputArea != null) {
                        inputArea.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        val areaLp = inputArea.layoutParams
                        areaLp.height = ViewGroup.LayoutParams.MATCH_PARENT
                        if (areaLp is FrameLayout.LayoutParams) {
                            areaLp.gravity = Gravity.FILL
                        } else if (areaLp is LinearLayout.LayoutParams) {
                            areaLp.gravity = Gravity.FILL
                            areaLp.weight = 1f
                        }
                        inputArea.layoutParams = areaLp
                    }
                    val parent = inputArea?.parent as? View
                    if (parent != null) {
                        parent.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        val pLp = parent.layoutParams
                        pLp?.height = ViewGroup.LayoutParams.MATCH_PARENT
                        parent.layoutParams = pLp
                    }
                    targetWindow.decorView.requestLayout()
                } catch (e: Exception) { /* ignore */ }
            }
        } else {
            // Standard Docked Mode
            targetWindow.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            targetWindow.setGravity(Gravity.BOTTOM)
            val lp = targetWindow.attributes
            lp.gravity = Gravity.BOTTOM
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.flags = lp.flags and
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.inv() and
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH.inv() and
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS.inv() and
                WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            targetWindow.attributes = lp

            targetWindow.decorView.post {
                try {
                    val inputArea = targetWindow.findViewById<View>(android.R.id.inputArea)
                    if (inputArea != null) {
                        inputArea.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        val areaLp = inputArea.layoutParams
                        areaLp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        if (areaLp is FrameLayout.LayoutParams) {
                            areaLp.gravity = Gravity.BOTTOM
                        } else if (areaLp is LinearLayout.LayoutParams) {
                            areaLp.gravity = Gravity.BOTTOM
                            areaLp.weight = 0f
                        }
                        inputArea.layoutParams = areaLp
                    }
                    val parent = inputArea?.parent as? View
                    if (parent != null) {
                        parent.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        val pLp = parent.layoutParams
                        pLp?.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        parent.layoutParams = pLp
                    }
                    targetWindow.decorView.requestLayout()
                } catch (e: Exception) { /* ignore */ }
            }
        }
    }

    private fun updateInputViewHeight(isFloating: Boolean) {
        val inputView = keyboardComposeView ?: return
        val layoutParams = inputView.layoutParams ?: return
        val height = if (isFloating) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        if (layoutParams.height != height) {
            layoutParams.height = height
            inputView.layoutParams = layoutParams
            inputView.requestLayout()
        }
    }

    private fun updateCurrentText() {
        try {
            val textBefore = currentInputConnection?.getTextBeforeCursor(40, 0)?.toString() ?: ""
            currentTextState.value = textBefore
        } catch (e: Exception) {
            currentTextState.value = ""
        }
    }

    override fun onCreateInputView(): View {
        installWindowViewTreeOwners()
        val composeView = ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (preferences.isFloating) {
                    ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
            )
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            installViewTreeOwners(this)
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@MBoardInputMethodService))
            setContent {
                MBoardKeyboardView(
                    preferences = preferences,
                    suggestionEngine = suggestionEngine,
                    currentInputText = currentTextState.value,
                    clipboardSuggestion = clipboardSuggestionState.value,
                    onClipboardSuggestionSelected = { text ->
                        currentInputConnection?.commitText(text, 1)
                        clipboardSuggestionState.value = null
                        updateCurrentText()
                    },
                    onCommitText = { text ->
                        commitTypedText(text)
                    },
                    onDelete = {
                        val ic = currentInputConnection
                        if (ic != null) {
                            val selected = ic.getSelectedText(0)
                            if (!selected.isNullOrEmpty()) {
                                ic.commitText("", 1)
                            } else {
                                val deleted = ic.deleteSurroundingText(1, 0)
                                if (!deleted) {
                                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                                }
                            }
                        } else {
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                        }
                        updateCurrentText()
                    },
                    onEnter = {
                        val editorInfo = currentInputEditorInfo
                        val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
                        val noEnterAction = editorInfo?.imeOptions?.and(EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
                        if (!noEnterAction && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                            currentInputConnection?.performEditorAction(action)
                            if (action == EditorInfo.IME_ACTION_DONE) requestHideSelf(0)
                        } else {
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                        }
                    },
                    enterLabel = enterLabelState.value,
                    onReplaceCurrentWord = { word ->
                        val inputConnection = currentInputConnection
                        val textBeforeCursor = inputConnection?.getTextBeforeCursor(100, 0)?.toString().orEmpty()
                        val currentWord = textBeforeCursor.takeLastWhile { !it.isWhitespace() }
                        if (currentWord.isNotEmpty()) {
                            inputConnection?.deleteSurroundingText(currentWord.length, 0)
                        }
                        inputConnection?.commitText("$word ", 1)
                        updateCurrentText()
                    },
                    onMoveCursor = { direction ->
                        when {
                            direction == -1 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
                            direction == 1 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
                            direction < -10 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP)
                            direction > 10 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
                        }
                        updateCurrentText()
                    },
                    onJumpStart = {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_HOME)
                        updateCurrentText()
                    },
                    onJumpEnd = {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_END)
                        updateCurrentText()
                    },
                    onSelectAll = {
                        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                    },
                    onCopy = {
                        currentInputConnection?.performContextMenuAction(android.R.id.copy)
                    },
                    onCut = {
                        currentInputConnection?.performContextMenuAction(android.R.id.cut)
                        updateCurrentText()
                    },
                    onPaste = {
                        currentInputConnection?.performContextMenuAction(android.R.id.paste)
                        updateCurrentText()
                    },
                    onOpenSettings = {
                        val intent = Intent(this@MBoardInputMethodService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    },
                    onHideKeyboard = {
                        requestHideSelf(0)
                    },
                    onFloatingToggle = { _ ->
                        updateInputViewHeight(preferences.isFloating)
                        updateWindowLayoutForFloating()
                    },
                    onPositionChanged = { rect ->
                        currentKeyboardBounds = rect
                        if (::preferences.isInitialized && preferences.isFloating) {
                            window?.window?.decorView?.let { decor ->
                                decor.post {
                                    decor.requestLayout()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                )
            }
        }
        keyboardComposeView = composeView
        return composeView
    }

    private fun enterLabelFor(info: EditorInfo?): String {
        val imeOptions = info?.imeOptions ?: return "↵"
        if (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return "↵"
        return when (imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO -> "Aller"
            EditorInfo.IME_ACTION_SEARCH -> "Rechercher"
            EditorInfo.IME_ACTION_SEND -> "Envoyer"
            EditorInfo.IME_ACTION_NEXT -> "Suivant"
            EditorInfo.IME_ACTION_DONE -> "Terminé"
            EditorInfo.IME_ACTION_PREVIOUS -> "Précédent"
            else -> "↵"
        }
    }
}
