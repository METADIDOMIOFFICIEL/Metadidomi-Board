package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardThemeColors

@Composable
fun KeyboardResizeOverlay(
    theme: KeyboardThemeColors,
    heightScale: Float,
    widthRatio: Float,
    bottomOffset: Int,
    onHeightScaleChange: (Float) -> Unit,
    onWidthRatioChange: (Float) -> Unit,
    onBottomOffsetChange: (Int) -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = theme.background.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1A73E8)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("keyboard_resize_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Redimensionner le clavier",
                    color = theme.keyTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Reset button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(theme.keyBackground)
                            .clickable { onReset() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("resize_reset_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Réinitialiser",
                                tint = theme.keyTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Défaut",
                                color = theme.keyTextColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Done / Validate button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1A73E8))
                            .clickable { onDone() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("resize_done_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Valider",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Valider",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Height Scale Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Hauteur : ${((heightScale * 100).toInt())}%",
                        color = theme.keyTextColor,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = heightScale,
                    onValueChange = onHeightScaleChange,
                    valueRange = 0.75f..1.30f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1A73E8),
                        activeTrackColor = Color(0xFF1A73E8),
                        inactiveTrackColor = theme.keySubTextColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }

            // Width Ratio Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Largeur : ${((widthRatio * 100).toInt())}%",
                        color = theme.keyTextColor,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = widthRatio,
                    onValueChange = onWidthRatioChange,
                    valueRange = 0.70f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1A73E8),
                        activeTrackColor = Color(0xFF1A73E8),
                        inactiveTrackColor = theme.keySubTextColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }

            // Bottom position / margin
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Position verticale : ${bottomOffset} dp",
                        color = theme.keyTextColor,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = bottomOffset.toFloat(),
                    onValueChange = { onBottomOffsetChange(it.toInt()) },
                    valueRange = 0f..48f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1A73E8),
                        activeTrackColor = Color(0xFF1A73E8),
                        inactiveTrackColor = theme.keySubTextColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}
