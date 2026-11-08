package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyHeightMode
import com.example.data.MBoardPreferences
import com.example.data.SuggestionEngine
import com.example.data.SuggestionResult
import com.example.ime.MBoardHaptics
import com.example.model.FontStyle
import com.example.model.FontTransformer
import com.example.model.KeyAction
import com.example.model.KeyModel
import com.example.model.KeyboardLayoutType
import com.example.model.KeyboardLayouts
import com.example.model.KeyboardMode
import com.example.model.KeyboardThemeColors
import com.example.model.KeyboardThemeId
import com.example.model.KeyboardThemes
import com.example.model.ShiftState
import kotlin.math.roundToInt

@Composable
fun MBoardKeyboardView(
    preferences: MBoardPreferences,
    suggestionEngine: SuggestionEngine,
    currentInputText: String = "",
    clipboardSuggestion: String? = null,
    onClipboardSuggestionSelected: (String) -> Unit = {},
    onCommitText: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    enterLabel: String = "↵",
    onReplaceCurrentWord: ((String) -> Unit)? = null,
    onMoveCursor: (Int) -> Unit = {},
    onJumpStart: () -> Unit = {},
    onJumpEnd: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onCopy: () -> Unit = {},
    onCut: () -> Unit = {},
    onPaste: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onHideKeyboard: () -> Unit = {},
    onFloatingToggle: ((Boolean) -> Unit)? = null,
    onPositionChanged: ((android.graphics.Rect) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(KeyboardMode.ALPHA) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Floating & Resizing states (Gboard style)
    var isFloating by remember { mutableStateOf(preferences.isFloating) }
    var isResizeMode by remember { mutableStateOf(false) }
    var isLayoutEditorVisible by remember { mutableStateOf(false) }
    var isTranslatorPanelVisible by remember { mutableStateOf(false) }
    var translatorInputText by remember { mutableStateOf("") }
    var emojiSearchActive by remember { mutableStateOf(false) }
    var emojiSearchQuery by remember { mutableStateOf("") }
    var customAlphaKeys by remember { mutableStateOf(preferences.customAlphaKeys) }

    var keyboardScale by remember { mutableFloatStateOf(preferences.keyboardScale) }
    var keyboardWidthRatio by remember { mutableFloatStateOf(preferences.keyboardWidthRatio) }
    var keyboardBottomOffset by remember { mutableIntStateOf(preferences.keyboardBottomOffset) }
    var floatingOffsetX by remember { mutableFloatStateOf(preferences.floatingPositionX) }
    var floatingOffsetY by remember { mutableFloatStateOf(preferences.floatingPositionY) }

    // Font style state (Unicode text transformation without color tampering)
    var isFontBarVisible by remember { mutableStateOf(false) }
    var selectedFontStyle by remember { mutableStateOf(preferences.activeFontStyle) }

    val theme = KeyboardThemes.getTheme(preferences.themeId)
    val layoutType = preferences.layoutType

    // Handle back button for secondary modes & tools if dispatcher is available
    val backDispatcherOwner = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current
    if (backDispatcherOwner != null) {
        BackHandler(enabled = mode != KeyboardMode.ALPHA || isResizeMode || isFontBarVisible || isLayoutEditorVisible || isTranslatorPanelVisible || emojiSearchActive) {
            when {
                emojiSearchActive -> {
                    emojiSearchActive = false
                    emojiSearchQuery = ""
                    mode = KeyboardMode.EMOJI
                }
                isLayoutEditorVisible -> isLayoutEditorVisible = false
                isResizeMode -> isResizeMode = false
                isFontBarVisible -> isFontBarVisible = false
                isTranslatorPanelVisible -> isTranslatorPanelVisible = false
                mode != KeyboardMode.ALPHA -> {
                    MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                    mode = KeyboardMode.ALPHA
                }
            }
        }
    }

    // Predictive suggestions with Gboard N-gram & DB model
    val suggestionInputText = when {
        isTranslatorPanelVisible -> translatorInputText
        emojiSearchActive -> emojiSearchQuery
        else -> currentInputText
    }
    val lastWord = remember(suggestionInputText) {
        val tokens = suggestionInputText.trimEnd().split("\\s+".toRegex())
        tokens.lastOrNull()?.takeIf { it.isNotBlank() }
    }
    val currentPrefix = remember(suggestionInputText) {
        if (suggestionInputText.isBlank() || suggestionInputText.last().isWhitespace()) "" else lastWord ?: ""
    }
    val suggestions = remember(currentPrefix, lastWord, suggestionInputText) {
        if (currentPrefix.isEmpty()) {
            val shouldPredictNextWord = suggestionInputText.isNotBlank() &&
                suggestionInputText.last().isWhitespace() &&
                preferences.nextWordPrediction
            if (shouldPredictNextWord) suggestionEngine.getSuggestions("", lastWord) else SuggestionResult(emptyList())
        } else {
            suggestionEngine.getSuggestions(currentPrefix, null)
        }
    }

    // Wrap committing text with active font style transformation
    val transformAndCommit: (String) -> Unit = { text ->
        val transformed = if (!isTranslatorPanelVisible && selectedFontStyle != FontStyle.NORMAL) {
            FontTransformer.transform(text, selectedFontStyle)
        } else {
            text
        }
        if (isTranslatorPanelVisible) {
            translatorInputText += transformed
        } else if (emojiSearchActive) {
            emojiSearchQuery += transformed
        } else {
            onCommitText(transformed)
        }
        if (!isTranslatorPanelVisible && !emojiSearchActive && text.length > 1 && !text.contains(" ")) {
            suggestionEngine.learnWord(text, lastWord)
        }
    }
    val deleteCurrentInput: () -> Unit = {
        if (isTranslatorPanelVisible) {
            if (translatorInputText.isNotEmpty()) {
                val lastCodePoint = translatorInputText.codePointBefore(translatorInputText.length)
                translatorInputText = translatorInputText.dropLast(Character.charCount(lastCodePoint))
            }
        } else if (emojiSearchActive) {
            if (emojiSearchQuery.isNotEmpty()) {
                val lastCodePoint = emojiSearchQuery.codePointBefore(emojiSearchQuery.length)
                emojiSearchQuery = emojiSearchQuery.dropLast(Character.charCount(lastCodePoint))
            }
        } else {
            onDelete()
        }
    }
    val enterCurrentInput: () -> Unit = {
        when {
            isTranslatorPanelVisible -> translatorInputText += "\n"
            emojiSearchActive -> emojiSearchQuery += " "
            else -> onEnter()
        }
    }
    val replaceCurrentWord: (String) -> Unit = { word ->
        if (isTranslatorPanelVisible) {
            translatorInputText = translatorInputText.dropLast(currentPrefix.length) + word + " "
        } else if (emojiSearchActive) {
            emojiSearchQuery = emojiSearchQuery.dropLast(currentPrefix.length) + word + " "
        } else if (onReplaceCurrentWord != null) {
            onReplaceCurrentWord(word)
        } else {
            repeat(currentPrefix.length) { onDelete() }
            onCommitText(word + " ")
        }
    }

    val hasBackgroundImage = !preferences.backgroundImageUri.isNullOrEmpty() || !preferences.backgroundImagePreset.isNullOrEmpty()

    var keyboardCardSize by remember { mutableStateOf(IntSize.Zero) }

    // Floating wrapper BoxWithConstraints allowing free placement anywhere on screen without boundary loss
    BoxWithConstraints(
        modifier = if (isFloating) {
            modifier.fillMaxSize()
        } else {
            modifier.fillMaxWidth()
        },
        contentAlignment = Alignment.BottomCenter
    ) {
        val density = LocalDensity.current
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()
        val kbW = if (keyboardCardSize.width > 0) keyboardCardSize.width.toFloat() else (maxW * preferences.floatingScale)
        val kbH = if (keyboardCardSize.height > 0) keyboardCardSize.height.toFloat() else with(density) { 270.dp.toPx() }
        // The keyboard is anchored to the bottom. Dragging up goes negative; dragging down is clamped at 0f (screen bottom)
        val maxDragUp = -(maxH - kbH - with(density) { 36.dp.toPx() }).coerceAtLeast(0f)
        val maxDragX = ((maxW - kbW) / 2f).coerceAtLeast(0f)

        // Keyboard Card container
        Surface(
            shape = if (isFloating) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp),
            shadowElevation = if (isFloating) 14.dp else 0.dp,
            border = if (isFloating) androidx.compose.foundation.BorderStroke(1.5.dp, theme.suggestionTextColor.copy(alpha = 0.5f)) else null,
            color = theme.background,
            modifier = (if (isFloating) {
                Modifier
                    .fillMaxWidth(preferences.floatingScale)
                    .offset {
                        val clampedX = floatingOffsetX.coerceIn(-maxDragX, maxDragX)
                        val clampedY = floatingOffsetY.coerceIn(maxDragUp, 0f)
                        IntOffset(clampedX.roundToInt(), clampedY.roundToInt())
                    }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            } else {
                Modifier
                    .fillMaxWidth(keyboardWidthRatio)
                    .padding(bottom = keyboardBottomOffset.dp)
            })
                .wrapContentHeight()
                .alpha(if (isFloating) preferences.floatingOpacity else 1f)
                .onGloballyPositioned { coordinates ->
                    if (coordinates.isAttached) {
                        keyboardCardSize = coordinates.size
                        val pos = coordinates.positionInWindow()
                        val size = coordinates.size
                        val rect = android.graphics.Rect(
                            pos.x.roundToInt(),
                            pos.y.roundToInt(),
                            (pos.x + size.width).roundToInt(),
                            (pos.y + size.height).roundToInt()
                        )
                        onPositionChanged?.invoke(rect)
                    }
                }
                .testTag("mboard_keyboard_container")
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background Image (Custom Photo or Preset Artwork like Gboard)
                if (hasBackgroundImage) {
                    KeyboardBackgroundView(
                        imageUri = preferences.backgroundImageUri,
                        preset = preferences.backgroundImagePreset,
                        darkness = preferences.backgroundOverlayDarkness
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Floating drag handle bar at TOP if floating
                    if (isFloating) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(theme.toolbarBackground.copy(alpha = 0.9f))
                                .pointerInput(maxW, maxH, kbW, kbH) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val newX = (floatingOffsetX + dragAmount.x).coerceIn(-maxDragX, maxDragX)
                                        val newY = (floatingOffsetY + dragAmount.y).coerceIn(maxDragUp, 0f)
                                        floatingOffsetX = newX
                                        floatingOffsetY = newY
                                        preferences.floatingPositionX = newX
                                        preferences.floatingPositionY = newY

                                        val baseLeft = ((maxW - kbW) / 2f) + newX
                                        val baseTop = (maxH - kbH) + newY
                                        val rect = android.graphics.Rect(
                                            baseLeft.roundToInt(),
                                            baseTop.roundToInt(),
                                            (baseLeft + kbW).roundToInt(),
                                            (baseTop + kbH).roundToInt()
                                        )
                                        onPositionChanged?.invoke(rect)
                                    }
                                }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Clavier Flottant (glissez pour déplacer)",
                                fontSize = 10.sp,
                                color = theme.suggestionTextColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Filled.DragHandle,
                                contentDescription = "Déplacer",
                                tint = theme.suggestionTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Gboard Top Toolbar
                    MBoardToolbar(
                        theme = theme,
                        suggestions = suggestions,
                        clipboardSuggestion = clipboardSuggestion,
                        onClipboardSuggestionClick = onClipboardSuggestionSelected,
                        currentLanguageLabel = when (layoutType) {
                            KeyboardLayoutType.AZERTY -> "FR"
                            KeyboardLayoutType.QWERTY -> "EN"
                            KeyboardLayoutType.QWERTZ -> "DE"
                        },
                        onSuggestionClick = { word ->
                            val transformed = if (!isTranslatorPanelVisible && selectedFontStyle != FontStyle.NORMAL) {
                                FontTransformer.transform(word, selectedFontStyle)
                            } else {
                                word
                            }
                            replaceCurrentWord(transformed)
                            if (!isTranslatorPanelVisible) suggestionEngine.learnWord(word, lastWord)
                        },
                        onEmojiClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            mode = KeyboardMode.EMOJI
                        },
                        onClipboardClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            mode = KeyboardMode.CLIPBOARD
                        },
                        onTextEditingClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            mode = KeyboardMode.TEXT_EDITING
                        },
                        onFontsClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            isFontBarVisible = !isFontBarVisible
                        },
                        onKidsClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            mode = KeyboardMode.KIDS
                        },
                        onHandwritingClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            mode = KeyboardMode.HANDWRITING
                        },
                        onCalculatorClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            mode = KeyboardMode.CALCULATOR
                        },
                        onTranslatorClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            isTranslatorPanelVisible = !isTranslatorPanelVisible
                        },
                        onFloatingToggle = {
                            val nextFloating = !isFloating
                            isFloating = nextFloating
                            preferences.isFloating = nextFloating
                            if (!nextFloating) {
                                floatingOffsetX = 0f
                                floatingOffsetY = 0f
                                preferences.floatingPositionX = 0f
                                preferences.floatingPositionY = 0f
                            }
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            onFloatingToggle?.invoke(nextFloating)
                        },
                        onResizeClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            isResizeMode = !isResizeMode
                        },
                        onLayoutReorganizeClick = {
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                            isLayoutEditorVisible = !isLayoutEditorVisible
                        },
                        isFloating = isFloating,
                        isFontActive = selectedFontStyle != FontStyle.NORMAL,
                        activeFontLabel = selectedFontStyle.displayName,
                        onThemeClick = onOpenSettings,
                        onLanguageClick = {
                            val nextLayout = when (layoutType) {
                                KeyboardLayoutType.AZERTY -> KeyboardLayoutType.QWERTY
                                KeyboardLayoutType.QWERTY -> KeyboardLayoutType.QWERTZ
                                KeyboardLayoutType.QWERTZ -> KeyboardLayoutType.AZERTY
                            }
                            preferences.layoutType = nextLayout
                            MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                        },
                        onVoiceClick = { showVoiceDialog = true },
                        onSettingsClick = onOpenSettings,
                        onHideKeyboard = onHideKeyboard
                    )

                    // Font Styles Bar (without custom colors)
                    AnimatedVisibility(visible = isFontBarVisible) {
                        FontStylesBar(
                            theme = theme,
                            selectedStyle = selectedFontStyle,
                            onStyleSelected = { style ->
                                selectedFontStyle = style
                                preferences.activeFontStyle = style
                                MBoardHaptics.performKeyClick(context, preferences.hapticFeedback, preferences.hapticStrength)
                            },
                            onClose = { isFontBarVisible = false }
                        )
                    }

                    AnimatedVisibility(visible = isTranslatorPanelVisible) {
                        TranslatorView(
                            theme = theme,
                            preferences = preferences,
                            sourceText = translatorInputText,
                            onSourceTextChange = { translatorInputText = it },
                            onCommitText = transformAndCommit,
                            onBackToAlpha = { isTranslatorPanelVisible = false },
                            compact = true
                        )
                    }

                    // Layout Editor Mode (Reorganize keys)
                    if (isLayoutEditorVisible) {
                        LayoutEditorComponent(
                            theme = theme,
                            currentOrder = customAlphaKeys ?: KeyboardLayouts.getDefaultOrder(layoutType),
                            layoutType = layoutType,
                            onSaveOrder = { newOrder ->
                                customAlphaKeys = newOrder
                                preferences.customAlphaKeys = newOrder
                            },
                            onClose = { isLayoutEditorVisible = false }
                        )
                    }

                    // Gboard Resize Overlay Mode
                    if (isResizeMode) {
                        KeyboardResizeOverlay(
                            theme = theme,
                            heightScale = keyboardScale,
                            widthRatio = keyboardWidthRatio,
                            bottomOffset = keyboardBottomOffset,
                            onHeightScaleChange = {
                                keyboardScale = it
                                preferences.keyboardScale = it
                            },
                            onWidthRatioChange = {
                                keyboardWidthRatio = it
                                preferences.keyboardWidthRatio = it
                            },
                            onBottomOffsetChange = {
                                keyboardBottomOffset = it
                                preferences.keyboardBottomOffset = it
                            },
                            onReset = {
                                keyboardScale = 1.0f
                                keyboardWidthRatio = 1.0f
                                keyboardBottomOffset = 0
                                preferences.keyboardScale = 1.0f
                                preferences.keyboardWidthRatio = 1.0f
                                preferences.keyboardBottomOffset = 0
                            },
                            onDone = { isResizeMode = false }
                        )
                    }

                    if (emojiSearchActive && !showVoiceDialog) {
                        EmojiSearchView(
                            theme = theme,
                            query = emojiSearchQuery,
                            onBack = {
                                emojiSearchActive = false
                                emojiSearchQuery = ""
                                mode = KeyboardMode.EMOJI
                            },
                            onEmojiSelected = { emoji ->
                                onCommitText(emoji)
                                emojiSearchActive = false
                                emojiSearchQuery = ""
                                mode = KeyboardMode.ALPHA
                            }
                        )
                    }

                    // Dynamic Body: Voice Dictation, Emoji, Clipboard, Text Editing, or Alpha/Symbols
                    if (showVoiceDialog) {
                        VoiceDictationSheet(
                            theme = theme,
                            preferences = preferences,
                            onTextRecognized = { text ->
                                val toCommit = if (text.endsWith(" ") || listOf(".", ",", "?", "!").contains(text)) text else "$text "
                                transformAndCommit(toCommit)
                            },
                            onDismiss = { showVoiceDialog = false }
                        )
                    } else {
                        when (mode) {
                            KeyboardMode.EMOJI -> {
                                EmojiPickerView(
                                    theme = theme,
                                    onEmojiSelected = { emoji ->
                                        transformAndCommit(emoji)
                                        mode = KeyboardMode.ALPHA
                                    },
                                    onSearchActivated = {
                                        emojiSearchActive = true
                                        emojiSearchQuery = ""
                                        mode = KeyboardMode.ALPHA
                                    },
                                    onBackspace = deleteCurrentInput,
                                    onBackToAlpha = {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        mode = KeyboardMode.ALPHA
                                    }
                                )
                            }
                            KeyboardMode.CLIPBOARD -> {
                                ClipboardView(
                                    theme = theme,
                                    preferences = preferences,
                                    onClipSelected = transformAndCommit,
                                    onBackToAlpha = {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        mode = KeyboardMode.ALPHA
                                    }
                                )
                            }
                            KeyboardMode.TEXT_EDITING -> {
                                TextEditingPadView(
                                    theme = theme,
                                    onMoveCursor = onMoveCursor,
                                    onJumpStart = onJumpStart,
                                    onJumpEnd = onJumpEnd,
                                    onSelectAll = onSelectAll,
                                    onCopy = onCopy,
                                    onCut = onCut,
                                    onPaste = onPaste,
                                    onBackToAlpha = {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        mode = KeyboardMode.ALPHA
                                    }
                                )
                            }
                            KeyboardMode.KIDS -> {
                                KidsKeyboardView(
                                    theme = theme,
                                    preferences = preferences,
                                    onCommitText = transformAndCommit,
                                    onDelete = deleteCurrentInput,
                                    onEnter = enterCurrentInput,
                                    onBackToAlpha = {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        mode = KeyboardMode.ALPHA
                                    }
                                )
                            }
                            KeyboardMode.HANDWRITING -> {
                                HandwritingKeyboardView(
                                    theme = theme,
                                    preferences = preferences,
                                    onCommitText = transformAndCommit,
                                    onDelete = deleteCurrentInput,
                                    onEnter = enterCurrentInput,
                                    onBackToAlpha = {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        mode = KeyboardMode.ALPHA
                                    }
                                )
                            }
                            KeyboardMode.CALCULATOR -> {
                                CalculatorKeyboardView(
                                    theme = theme,
                                    preferences = preferences,
                                    onCommitText = transformAndCommit,
                                    onBackToAlpha = {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        mode = KeyboardMode.ALPHA
                                    }
                                )
                            }
                            KeyboardMode.TRANSLATOR -> {
                                TranslatorView(
                                    theme = theme,
                                    preferences = preferences,
                                    sourceText = translatorInputText,
                                    onSourceTextChange = { translatorInputText = it },
                                    onCommitText = transformAndCommit,
                                    onBackToAlpha = {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        mode = KeyboardMode.ALPHA
                                    },
                                    compact = false
                                )
                            }
                            KeyboardMode.ALPHA,
                            KeyboardMode.NUMBERS_SYMBOLS,
                            KeyboardMode.MORE_SYMBOLS -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    // Optional dedicated number row
                                    if (preferences.showNumberRow && mode == KeyboardMode.ALPHA) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            KeyboardLayouts.NUMBER_ROW.forEach { num ->
                                                KeyView(
                                                    key = KeyModel(KeyAction.Character(num), num, weight = 1f),
                                                    theme = theme,
                                                    shiftState = shiftState,
                                                    keyHeightMode = preferences.keyHeight,
                                                    showBorders = preferences.showKeyBorders,
                                                    enablePopup = preferences.keyPopupEnabled,
                                                    enableHaptics = preferences.hapticFeedback,
                                                    enableSound = preferences.soundFeedback,
                                                    soundVolume = preferences.soundVolume,
                                                    hapticStrength = preferences.hapticStrength,
                                                    modifier = Modifier.weight(1f),
                                                    onAction = { transformAndCommit(num) }
                                                )
                                            }
                                        }
                                    }

                                    // Main Rows (with custom key positions if configured)
                                    val rows = when (mode) {
                                        KeyboardMode.ALPHA -> KeyboardLayouts.getAlphaRows(layoutType, shiftState, customAlphaKeys)
                                        KeyboardMode.NUMBERS_SYMBOLS -> KeyboardLayouts.getSymbols1Rows()
                                        KeyboardMode.MORE_SYMBOLS -> KeyboardLayouts.getSymbols2Rows()
                                        else -> emptyList()
                                    }

                                    rows.forEach { rowKeys ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            rowKeys.forEach { keyModel ->
                                                KeyView(
                                                    key = keyModel,
                                                    theme = theme,
                                                    shiftState = shiftState,
                                                    keyHeightMode = preferences.keyHeight,
                                                    showBorders = preferences.showKeyBorders,
                                                    enablePopup = preferences.keyPopupEnabled,
                                                    enableHaptics = preferences.hapticFeedback,
                                                    enableSound = preferences.soundFeedback,
                                                    soundVolume = preferences.soundVolume,
                                                    hapticStrength = preferences.hapticStrength,
                                                    modifier = Modifier.weight(keyModel.weight),
                                                    onAction = { action ->
                                                        handleKeyAction(
                                                            action = action,
                                                            shiftState = shiftState,
                                                            onShiftChange = { shiftState = it },
                                                            onModeChange = { mode = it },
                                                            onCommitText = transformAndCommit,
                                                            onDelete = deleteCurrentInput,
                                                            onEnter = enterCurrentInput
                                                        )
                                                    },
                                                    onAccentSelected = { accent ->
                                                        transformAndCommit(accent)
                                                        if (shiftState == ShiftState.ON) {
                                                            shiftState = ShiftState.OFF
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Bottom spacebar row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mode Switch Key (?123 / ABC)
                                        val modeKeyModel = if (mode == KeyboardMode.ALPHA) {
                                            KeyModel(KeyAction.SwitchMode(KeyboardMode.NUMBERS_SYMBOLS), "?123", weight = 1.35f, isSpecial = true)
                                        } else {
                                            KeyModel(KeyAction.SwitchMode(KeyboardMode.ALPHA), "ABC", weight = 1.35f, isSpecial = true)
                                        }
                                        KeyView(
                                            key = modeKeyModel,
                                            theme = theme,
                                            shiftState = shiftState,
                                            keyHeightMode = preferences.keyHeight,
                                            showBorders = preferences.showKeyBorders,
                                            enablePopup = preferences.keyPopupEnabled,
                                            enableHaptics = preferences.hapticFeedback,
                                            enableSound = preferences.soundFeedback,
                                            soundVolume = preferences.soundVolume,
                                            hapticStrength = preferences.hapticStrength,
                                            modifier = Modifier.weight(modeKeyModel.weight),
                                            onAction = { action ->
                                                if (action is KeyAction.SwitchMode) {
                                                    MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                                    mode = action.targetMode
                                                }
                                            }
                                        )

                                        // Emoji key
                                        KeyView(
                                            key = KeyModel(KeyAction.Emoji, "😊", weight = 1.0f, isSpecial = true),
                                            theme = theme,
                                            shiftState = shiftState,
                                            keyHeightMode = preferences.keyHeight,
                                            showBorders = preferences.showKeyBorders,
                                            enablePopup = preferences.keyPopupEnabled,
                                            enableHaptics = preferences.hapticFeedback,
                                            enableSound = preferences.soundFeedback,
                                            soundVolume = preferences.soundVolume,
                                            hapticStrength = preferences.hapticStrength,
                                            modifier = Modifier.weight(1.0f),
                                            onAction = {
                                                MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                                mode = KeyboardMode.EMOJI
                                            }
                                        )

                                        // Spacebar (large)
                                        KeyView(
                                            key = KeyModel(KeyAction.Space, " ", weight = 4.3f),
                                            theme = theme,
                                            shiftState = shiftState,
                                            keyHeightMode = preferences.keyHeight,
                                            showBorders = preferences.showKeyBorders,
                                            enablePopup = preferences.keyPopupEnabled,
                                            enableHaptics = preferences.hapticFeedback,
                                            enableSound = preferences.soundFeedback,
                                            soundVolume = preferences.soundVolume,
                                            hapticStrength = preferences.hapticStrength,
                                            modifier = Modifier.weight(4.3f),
                                            onAction = { transformAndCommit(" ") }
                                        )

                                        // Period / Comma key
                                        KeyView(
                                            key = KeyModel(KeyAction.Character("."), ".", weight = 1.0f),
                                            theme = theme,
                                            shiftState = shiftState,
                                            keyHeightMode = preferences.keyHeight,
                                            showBorders = preferences.showKeyBorders,
                                            enablePopup = preferences.keyPopupEnabled,
                                            enableHaptics = preferences.hapticFeedback,
                                            enableSound = preferences.soundFeedback,
                                            soundVolume = preferences.soundVolume,
                                            hapticStrength = preferences.hapticStrength,
                                            modifier = Modifier.weight(1.0f),
                                            onAction = { transformAndCommit(".") }
                                        )

                                        // Enter action key (featuring the Cameroon gold star if in Cameroon theme!)
                                        val keyEnterLabel = if (enterLabel == "↵" && theme.id == KeyboardThemeId.CAMEROON) "★" else enterLabel
                                        KeyView(
                                            key = KeyModel(KeyAction.Enter, keyEnterLabel, weight = 1.35f, isSpecial = true),
                                            theme = theme,
                                            shiftState = shiftState,
                                            keyHeightMode = preferences.keyHeight,
                                            showBorders = preferences.showKeyBorders,
                                            enablePopup = preferences.keyPopupEnabled,
                                            enableHaptics = preferences.hapticFeedback,
                                            enableSound = preferences.soundFeedback,
                                            soundVolume = preferences.soundVolume,
                                            hapticStrength = preferences.hapticStrength,
                                            modifier = Modifier.weight(1.35f),
                                            onAction = { enterCurrentInput() }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Gboard Floating Drag Handle Bar (displayed at the bottom when floating)
                    if (isFloating) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(theme.toolbarBackground)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Dock button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.keyBackground)
                                    .clickable {
                                        isFloating = false
                                        preferences.isFloating = false
                                        floatingOffsetX = 0f
                                        floatingOffsetY = 0f
                                        preferences.floatingPositionX = 0f
                                        preferences.floatingPositionY = 0f
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        onFloatingToggle?.invoke(false)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("dock_floating_keyboard"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerticalAlignBottom,
                                        contentDescription = "Ancrer",
                                        tint = theme.keyTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("Ancrer", color = theme.keyTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Quick button: Snap directly to the bottom of the screen!
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.keyBackground)
                                    .clickable {
                                        floatingOffsetY = 0f
                                        floatingOffsetX = 0f
                                        preferences.floatingPositionX = 0f
                                        preferences.floatingPositionY = 0f
                                        val baseLeft = ((maxW - kbW) / 2f)
                                        val baseTop = (maxH - kbH)
                                        val rect = android.graphics.Rect(
                                            baseLeft.roundToInt(),
                                            baseTop.roundToInt(),
                                            (baseLeft + kbW).roundToInt(),
                                            (baseTop + kbH).roundToInt()
                                        )
                                        onPositionChanged?.invoke(rect)
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("snap_bottom_floating_keyboard"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Placer en bas",
                                        tint = theme.keyTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("En bas", color = theme.keyTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Central Draggable Grip Handle (Drags freely anywhere on screen)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .pointerInput(maxW, maxH, kbW, kbH) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val newX = (floatingOffsetX + dragAmount.x).coerceIn(-maxDragX, maxDragX)
                                            val newY = (floatingOffsetY + dragAmount.y).coerceIn(maxDragUp, 0f)
                                            floatingOffsetX = newX
                                            floatingOffsetY = newY
                                            preferences.floatingPositionX = newX
                                            preferences.floatingPositionY = newY

                                            val baseLeft = ((maxW - kbW) / 2f) + newX
                                            val baseTop = (maxH - kbH) + newY
                                            val rect = android.graphics.Rect(
                                                baseLeft.roundToInt(),
                                                baseTop.roundToInt(),
                                                (baseLeft + kbW).roundToInt(),
                                                (baseTop + kbH).roundToInt()
                                            )
                                            onPositionChanged?.invoke(rect)
                                        }
                                    }
                                    .testTag("floating_drag_handle"),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DragHandle,
                                    contentDescription = "Déplacer le clavier",
                                    tint = theme.suggestionTextColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Resize mode toggle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isResizeMode) theme.actionKeyBackground else theme.keyBackground)
                                    .clickable {
                                        MBoardHaptics.performModeSwitch(context, preferences.hapticFeedback, preferences.hapticStrength)
                                        isResizeMode = !isResizeMode
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("resize_floating_keyboard"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AspectRatio,
                                    contentDescription = "Taille",
                                    tint = if (isResizeMode) theme.actionKeyTextColor else theme.keyTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleKeyAction(
    action: KeyAction,
    shiftState: ShiftState,
    onShiftChange: (ShiftState) -> Unit,
    onModeChange: (KeyboardMode) -> Unit,
    onCommitText: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit
) {
    when (action) {
        is KeyAction.Character -> {
            onCommitText(action.primary)
            if (shiftState == ShiftState.ON) {
                onShiftChange(ShiftState.OFF)
            }
        }
        is KeyAction.Shift -> {
            val nextShift = when (shiftState) {
                ShiftState.OFF -> ShiftState.ON
                ShiftState.ON -> ShiftState.CAPS_LOCK
                ShiftState.CAPS_LOCK -> ShiftState.OFF
            }
            onShiftChange(nextShift)
        }
        is KeyAction.Backspace -> {
            onDelete()
        }
        is KeyAction.Space -> {
            onCommitText(" ")
        }
        is KeyAction.Enter -> {
            onEnter()
        }
        is KeyAction.SwitchMode -> {
            onModeChange(action.targetMode)
        }
        is KeyAction.Emoji -> {
            onModeChange(KeyboardMode.EMOJI)
        }
        is KeyAction.Clipboard -> {
            onModeChange(KeyboardMode.CLIPBOARD)
        }
        is KeyAction.TextEditing -> {
            onModeChange(KeyboardMode.TEXT_EDITING)
        }
        else -> {}
    }
}
