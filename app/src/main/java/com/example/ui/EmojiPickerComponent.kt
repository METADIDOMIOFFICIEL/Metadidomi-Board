package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmojiData
import com.example.model.KeyboardThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmojiPickerView(
    theme: KeyboardThemeColors,
    onEmojiSelected: (String) -> Unit,
    onSearchActivated: () -> Unit,
    onBackspace: () -> Unit,
    onBackToAlpha: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val categories = EmojiData.CATEGORIES
    val currentCategory = categories.getOrNull(selectedCategoryIndex) ?: categories.first()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(theme.background)
    ) {
        // Search & category strip with prominent EXIT BACK button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Prominent Back to ABC button on top
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.actionKeyBackground)
                    .clickable { onBackToAlpha() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("emoji_top_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux lettres",
                        tint = theme.actionKeyTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ABC",
                        color = theme.actionKeyTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Tapping the search control opens the dedicated search panel.
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.keyBackground)
                    .border(1.dp, theme.keySubTextColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp)
                    .clickable(onClick = onSearchActivated)
                    .testTag("emoji_search_input"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Recherche",
                        tint = theme.keySubTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Rechercher un émoji...",
                        color = theme.keySubTextColor,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
            }
        }

        // Categories selector with quick Return to ABC
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sticky Return chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.specialKeyBackground)
                    .clickable { onBackToAlpha() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌨ ABC",
                    color = theme.specialKeyTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            categories.forEachIndexed { index, cat ->
                val isSelected = index == selectedCategoryIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) theme.actionKeyBackground else Color.Transparent)
                        .clickable {
                            selectedCategoryIndex = index
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${cat.icon} ${cat.name}",
                        color = if (isSelected) theme.actionKeyTextColor else theme.keyTextColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Emoji Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        ) {
            val isKaomoji = currentCategory.name == "Kaomojis"
            LazyVerticalGrid(
                columns = if (isKaomoji) GridCells.Fixed(3) else GridCells.Fixed(8),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize().testTag("emoji_grid")
            ) {
                items(currentCategory.emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEmojiSelected(emoji) }
                            .padding(vertical = if (isKaomoji) 8.dp else 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = if (isKaomoji) 13.sp else 22.sp,
                            color = theme.keyTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }
        }

        // Bottom row (ABC switch + Space + Backspace with auto-repeat)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(theme.toolbarBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Big clear ABC button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.specialKeyBackground)
                    .clickable { onBackToAlpha() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("emoji_back_to_abc"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "Clavier ABC",
                        tint = theme.specialKeyTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ABC",
                        color = theme.specialKeyTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Space key in emoji mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.keyBackground)
                    .clickable { onEmojiSelected(" ") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "espace",
                    color = theme.keyTextColor.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            // Backspace button with immediate press & auto-repeat
            var isDeletePressed by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDeletePressed) theme.specialKeyBackground.copy(alpha = 0.7f) else theme.specialKeyBackground)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isDeletePressed = true
                                onBackspace()
                                val repeatJob = scope.launch {
                                    delay(380)
                                    while (isDeletePressed) {
                                        onBackspace()
                                        delay(55)
                                    }
                                }
                                tryAwaitRelease()
                                repeatJob.cancel()
                                isDeletePressed = false
                            }
                        )
                    }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("emoji_backspace"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Effacer",
                    tint = theme.specialKeyTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
