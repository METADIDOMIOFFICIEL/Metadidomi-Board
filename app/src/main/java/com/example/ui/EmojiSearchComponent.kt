package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmojiData
import com.example.model.KeyboardThemeColors

@Composable
fun EmojiSearchView(
    theme: KeyboardThemeColors,
    query: String,
    onBack: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedQuery = query.trim().lowercase()
    val results = remember(normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            emptyList()
        } else {
            EmojiData.KEYWORD_EMOJIS
                .filter { it.key.contains(normalizedQuery) }
                .values
                .flatten()
                .distinct()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(theme.background)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(theme.keyBackground)
                .border(1.dp, theme.keySubTextColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp)
                .testTag("emoji_search_field"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Recherche d’émojis",
                tint = theme.suggestionTextColor,
                modifier = Modifier.size(16.dp)
            )
            BasicTextField(
                value = query,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                maxLines = 1,
                textStyle = TextStyle(color = theme.keyTextColor, fontSize = 13.sp),
                cursorBrush = SolidColor(theme.suggestionTextColor),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("emoji_search_query"),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Écrire pour rechercher un émoji...",
                                color = theme.keySubTextColor,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour aux catégories",
                    tint = theme.keySubTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (normalizedQuery.isEmpty()) {
            Text(
                text = "Saisissez un mot avec le clavier pour afficher les émojis correspondants.",
                color = theme.keySubTextColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        } else if (results.isEmpty()) {
            Text(
                text = "Aucun émoji trouvé",
                color = theme.keySubTextColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                results.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEmojiSelected(emoji) }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}
