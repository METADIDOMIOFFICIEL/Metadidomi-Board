package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SuggestionResult
import com.example.R
import com.example.model.KeyboardThemeColors

@Composable
fun MBoardToolbar(
    theme: KeyboardThemeColors,
    suggestions: SuggestionResult,
    clipboardSuggestion: String? = null,
    onClipboardSuggestionClick: (String) -> Unit = {},
    currentLanguageLabel: String,
    onSuggestionClick: (String) -> Unit,
    onEmojiClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onTextEditingClick: () -> Unit,
    onFontsClick: () -> Unit,
    onKidsClick: () -> Unit = {},
    onHandwritingClick: () -> Unit = {},
    onCalculatorClick: () -> Unit = {},
    onTranslatorClick: () -> Unit = {},
    onFloatingToggle: () -> Unit,
    onResizeClick: () -> Unit,
    onLayoutReorganizeClick: () -> Unit = {},
    isFloating: Boolean,
    isFontActive: Boolean,
    activeFontLabel: String?,
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHideKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isToolsDrawerOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(theme.toolbarBackground)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // MBoard Logo button / Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isToolsDrawerOpen) theme.actionKeyBackground else theme.keyBackground)
                    .clickable { isToolsDrawerOpen = !isToolsDrawerOpen }
                    .testTag("toolbar_toggle"),
                contentAlignment = Alignment.Center
            ) {
                if (isToolsDrawerOpen) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Fermer les outils",
                        tint = if (isToolsDrawerOpen) theme.actionKeyTextColor else theme.keyTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.logo_mboard),
                        contentDescription = "MBoard",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Quick Fonts badge if a custom font style is active
            if (isFontActive && !isToolsDrawerOpen) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.actionKeyBackground)
                        .clickable { onFontsClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("active_font_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "𝓕 ${activeFontLabel ?: "Police"}",
                        color = theme.actionKeyTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedContent(
                targetState = isToolsDrawerOpen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "toolbar_content",
                modifier = Modifier.weight(1f)
            ) { openTools ->
                if (openTools) {
                    // Quick Tools Row (Gboard style drawer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolIconButton(
                            icon = Icons.Filled.Translate,
                            label = "Traducteur",
                            tint = Color(0xFF1E88E5),
                            onClick = {
                                onTranslatorClick()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_translator"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.Calculate,
                            label = "Calculatrice",
                            tint = theme.suggestionTextColor,
                            onClick = {
                                onCalculatorClick()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_calc"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.Face,
                            label = "Clavier Enfant",
                            tint = Color(0xFFFF9800),
                            onClick = {
                                onKidsClick()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_kids"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.Gesture,
                            label = "Écriture manuscrite",
                            tint = theme.keyTextColor,
                            onClick = {
                                onHandwritingClick()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_handwriting"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.TextFields,
                            label = "Styles d'écriture",
                            tint = if (isFontActive) theme.suggestionTextColor else theme.keyTextColor,
                            onClick = {
                                onFontsClick()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_fonts"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.PictureInPicture,
                            label = if (isFloating) "Ancrer" else "Flottant",
                            tint = if (isFloating) theme.actionKeyBackground else theme.keyTextColor,
                            onClick = {
                                onFloatingToggle()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_floating"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.AspectRatio,
                            label = "Redimensionner",
                            tint = theme.keyTextColor,
                            onClick = {
                                onResizeClick()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_resize"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.OpenWith,
                            label = "Réorganiser touches",
                            tint = theme.suggestionTextColor,
                            onClick = {
                                onLayoutReorganizeClick()
                                isToolsDrawerOpen = false
                            },
                            testTag = "tool_reorganize"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.EmojiEmotions,
                            label = "Émoticônes",
                            tint = theme.keyTextColor,
                            onClick = onEmojiClick,
                            testTag = "tool_emoji"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.ContentPaste,
                            label = "Presse-papiers",
                            tint = theme.keyTextColor,
                            onClick = onClipboardClick,
                            testTag = "tool_clipboard"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.OpenWith,
                            label = "Édition texte",
                            tint = theme.keyTextColor,
                            onClick = onTextEditingClick,
                            testTag = "tool_cursor"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.ColorLens,
                            label = "Thèmes",
                            tint = theme.keyTextColor,
                            onClick = onThemeClick,
                            testTag = "tool_theme"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.Language,
                            label = "Changer la disposition du clavier ($currentLanguageLabel)",
                            tint = theme.keyTextColor,
                            onClick = onLanguageClick,
                            testTag = "tool_lang"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.Mic,
                            label = "Dictée vocale",
                            tint = theme.actionKeyBackground,
                            onClick = onVoiceClick,
                            testTag = "tool_mic"
                        )
                        ToolIconButton(
                            icon = Icons.Filled.Settings,
                            label = "Paramètres",
                            tint = theme.keyTextColor,
                            onClick = onSettingsClick,
                            testTag = "tool_settings"
                        )
                    }
                } else {
                    // Suggestions Strip (Predictive text + Emoji candidate with horizontal scroll)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val words = suggestions.words
                        words.forEachIndexed { index, word ->
                            val isPrimary = index == 0
                            Box(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .fillMaxHeight(0.78f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPrimary) theme.actionKeyBackground.copy(alpha = 0.28f) else theme.keyBackground.copy(alpha = 0.6f))
                                    .clickable { onSuggestionClick(word) }
                                    .padding(horizontal = 12.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = word,
                                    color = if (isPrimary) theme.suggestionTextColor else theme.keyTextColor,
                                    fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.5.sp,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Emoji suggestion chip if detected
                        if (suggestions.emoji != null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.keyBackground)
                                    .clickable { onSuggestionClick(suggestions.emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = suggestions.emoji,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        if (!clipboardSuggestion.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .fillMaxHeight(0.78f)
                                    .widthIn(max = 180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.keyBackground)
                                    .clickable { onClipboardSuggestionClick(clipboardSuggestion) }
                                    .padding(horizontal = 8.dp)
                                    .testTag("clipboard_suggestion"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentPaste,
                                    contentDescription = "Texte copié",
                                    tint = theme.suggestionTextColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = clipboardSuggestion.replace('\n', ' '),
                                    color = theme.keyTextColor,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Quick Gboard Voice Mic Button (Direct access on toolbar like Gboard)
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("toolbar_voice_quick")
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Dictée vocale",
                    tint = theme.suggestionTextColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Hide keyboard button
            IconButton(
                onClick = onHideKeyboard,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("toolbar_hide")
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardHide,
                    contentDescription = "Réduire le clavier",
                    tint = theme.keySubTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolIconButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(34.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
