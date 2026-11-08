package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.KeyHeightMode
import com.example.model.KeyAction
import com.example.model.KeyModel
import com.example.model.KeyboardThemeColors
import com.example.model.ShiftState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KeyView(
    key: KeyModel,
    theme: KeyboardThemeColors,
    shiftState: ShiftState,
    keyHeightMode: KeyHeightMode,
    showBorders: Boolean,
    enablePopup: Boolean,
    enableHaptics: Boolean,
    enableSound: Boolean,
    hapticStrength: Float = 0.5f,
    soundVolume: Float = 0.6f,
    modifier: Modifier = Modifier,
    onAction: (KeyAction) -> Unit,
    onAccentSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showAccentPopup by remember { mutableStateOf(false) }
    var isPressedState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val baseHeight = when (keyHeightMode) {
        KeyHeightMode.COMPACT -> 42.dp
        KeyHeightMode.NORMAL -> 48.dp
        KeyHeightMode.TALL -> 54.dp
    }

    fun playFeedback() {
        if (enableSound) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                val fx = when (key.action) {
                    is KeyAction.Backspace -> AudioManager.FX_KEYPRESS_DELETE
                    is KeyAction.Enter -> AudioManager.FX_KEYPRESS_RETURN
                    is KeyAction.Space -> AudioManager.FX_KEYPRESS_SPACEBAR
                    else -> AudioManager.FX_KEYPRESS_STANDARD
                }
                audioManager?.playSoundEffect(fx, soundVolume)
            } catch (e: Exception) { /* ignore */ }
        }
        if (enableHaptics) {
            when {
                key.action is KeyAction.SwitchMode || key.action is KeyAction.Shift || key.action is KeyAction.Emoji -> {
                    com.example.ime.MBoardHaptics.performModeSwitch(context, true, hapticStrength)
                }
                key.action is KeyAction.Enter -> {
                    com.example.ime.MBoardHaptics.performEnterClick(context, true, hapticStrength)
                }
                key.action is KeyAction.Space -> {
                    com.example.ime.MBoardHaptics.performSpaceClick(context, true, hapticStrength)
                }
                key.action is KeyAction.Backspace -> {
                    com.example.ime.MBoardHaptics.performBackspaceClick(context, true, hapticStrength)
                }
                else -> {
                    com.example.ime.MBoardHaptics.performKeyClick(context, true, hapticStrength)
                }
            }
        }
    }

    val keyBackground = when {
        key.action is KeyAction.Enter -> theme.actionKeyBackground
        key.isSpecial -> theme.specialKeyBackground
        else -> theme.keyBackground
    }

    val contentColor = when {
        key.action is KeyAction.Enter -> theme.actionKeyTextColor
        key.isSpecial -> theme.specialKeyTextColor
        else -> theme.keyTextColor
    }

    Box(
        modifier = modifier
            .padding(horizontal = 2.5.dp, vertical = 3.dp)
            .height(baseHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isPressedState) keyBackground.copy(alpha = 0.7f) else keyBackground
            )
            .then(
                if (showBorders) {
                    Modifier.border(
                        0.8.dp,
                        if (theme.isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                        RoundedCornerShape(6.dp)
                    )
                } else Modifier
            )
            .pointerInput(key) {
                if (key.action is KeyAction.Backspace) {
                    detectTapGestures(
                        onPress = {
                            isPressedState = true
                            playFeedback()
                            onAction(KeyAction.Backspace)
                            val job = scope.launch {
                                delay(380)
                                while (isPressedState) {
                                    playFeedback()
                                    onAction(KeyAction.Backspace)
                                    delay(55)
                                }
                            }
                            tryAwaitRelease()
                            job.cancel()
                            isPressedState = false
                        }
                    )
                } else {
                    detectTapGestures(
                        onPress = {
                            isPressedState = true
                            playFeedback()
                            tryAwaitRelease()
                            isPressedState = false
                        },
                        onTap = {
                            onAction(key.action)
                        },
                        onLongPress = {
                            if (key.action is KeyAction.Character && key.action.accents.isNotEmpty()) {
                                showAccentPopup = true
                            }
                        }
                    )
                }
            }
            .testTag("key_${key.label}"),
        contentAlignment = Alignment.Center
    ) {
        // Sub-label (number hint) if present
        if (key.subLabel != null) {
            Text(
                text = key.subLabel,
                color = theme.keySubTextColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 2.dp)
            )
        }

        // Main key content
        when (key.action) {
            is KeyAction.Shift -> {
                val icon = when (shiftState) {
                    ShiftState.CAPS_LOCK -> Icons.Filled.KeyboardCapslock
                    ShiftState.ON -> Icons.Filled.ArrowUpward
                    ShiftState.OFF -> Icons.Filled.ArrowUpward
                }
                val iconTint = if (shiftState != ShiftState.OFF) theme.suggestionTextColor else contentColor
                Icon(
                    imageVector = icon,
                    contentDescription = "Shift",
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            is KeyAction.Backspace -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Effacer",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            is KeyAction.Enter -> {
                if (key.label == "Terminé") {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Terminé",
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (key.label == "↵" || key.label == "★") {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = "Entrée",
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = key.label,
                        color = contentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            is KeyAction.Space -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "espace",
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is KeyAction.Emoji -> {
                Icon(
                    imageVector = Icons.Filled.EmojiEmotions,
                    contentDescription = "Emojis",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            else -> {
                Text(
                    text = key.label,
                    color = contentColor,
                    fontSize = if (key.label.length > 2) 13.sp else 18.sp,
                    fontWeight = if (key.isSpecial) FontWeight.Medium else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Keystroke popup preview (Gboard style magnifying preview bubble on tap)
        if (enablePopup && isPressedState && key.action is KeyAction.Character && !showAccentPopup) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -118),
                properties = PopupProperties(focusable = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.keyBackground,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, theme.suggestionTextColor.copy(alpha = 0.45f)),
                    modifier = Modifier.size(width = 54.dp, height = 54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = key.label,
                            color = theme.keyTextColor,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Accent popup
        if (showAccentPopup && key.action is KeyAction.Character && key.action.accents.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -110),
                onDismissRequest = { showAccentPopup = false },
                properties = PopupProperties(focusable = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.popupBackground,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.suggestionTextColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        key.action.accents.forEach { accentChar ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.keyBackground)
                                    .clickable {
                                        onAccentSelected(accentChar)
                                        showAccentPopup = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = accentChar,
                                    color = theme.keyTextColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
