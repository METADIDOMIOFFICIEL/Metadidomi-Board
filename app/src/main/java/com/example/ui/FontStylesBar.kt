package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FontStyle
import com.example.model.KeyboardThemeColors

@Composable
fun FontStylesBar(
    theme: KeyboardThemeColors,
    selectedStyle: FontStyle,
    onStyleSelected: (FontStyle) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(theme.toolbarBackground)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Close button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(theme.keyBackground)
                .clickable { onClose() }
                .testTag("close_font_bar"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Fermer les polices",
                tint = theme.keySubTextColor,
                modifier = Modifier.size(16.dp)
            )
        }

        // Horizontal list of styles
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FontStyle.values().forEach { style ->
                val isSelected = style == selectedStyle
                val chipBg = if (isSelected) theme.actionKeyBackground else theme.keyBackground
                val textColor = if (isSelected) theme.actionKeyTextColor else theme.keyTextColor

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .then(
                            if (isSelected) {
                                Modifier.border(1.dp, theme.suggestionTextColor, RoundedCornerShape(8.dp))
                            } else Modifier
                        )
                        .clickable { onStyleSelected(style) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("font_chip_${style.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${style.previewText} (${style.displayName})",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
