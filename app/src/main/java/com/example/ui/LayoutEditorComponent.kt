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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardLayoutType
import com.example.model.KeyboardLayouts
import com.example.model.KeyboardThemeColors

@Composable
fun LayoutEditorComponent(
    theme: KeyboardThemeColors,
    currentOrder: String,
    layoutType: KeyboardLayoutType,
    onSaveOrder: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var workingOrder by remember { mutableStateOf(currentOrder) }
    var selectedKeyForSwap by remember { mutableStateOf<String?>(null) }

    val rows = remember(workingOrder) {
        val parts = workingOrder.split("|")
        if (parts.size == 3) {
            listOf(
                parts[0].split(",").map { it.trim() },
                parts[1].split(",").map { it.trim() },
                parts[2].split(",").map { it.trim() }
            )
        } else {
            emptyList()
        }
    }

    fun swapKeys(k1: String, k2: String) {
        val flatList = rows.flatten().toMutableList()
        val idx1 = flatList.indexOf(k1)
        val idx2 = flatList.indexOf(k2)
        if (idx1 != -1 && idx2 != -1) {
            flatList[idx1] = k2
            flatList[idx2] = k1

            val r1Size = rows[0].size
            val r2Size = rows[1].size
            val newR1 = flatList.subList(0, r1Size).joinToString(",")
            val newR2 = flatList.subList(r1Size, r1Size + r2Size).joinToString(",")
            val newR3 = flatList.subList(r1Size + r2Size, flatList.size).joinToString(",")
            workingOrder = "$newR1|$newR2|$newR3"
        }
        selectedKeyForSwap = null
    }

    Surface(
        color = theme.background.copy(alpha = 0.98f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, theme.suggestionTextColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("layout_editor_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Réorganiser les touches",
                        fontWeight = FontWeight.Bold,
                        color = theme.keyTextColor,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (selectedKeyForSwap != null) "Touchez une 2e touche pour l'échanger avec '$selectedKeyForSwap'" else "Touchez 2 touches successivement pour permuter leur position",
                        color = if (selectedKeyForSwap != null) theme.suggestionTextColor else theme.keySubTextColor,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Reset to default
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(theme.keyBackground)
                            .clickable {
                                workingOrder = KeyboardLayouts.getDefaultOrder(layoutType)
                                selectedKeyForSwap = null
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
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
                                modifier = Modifier.size(13.dp)
                            )
                            Text("Défaut", color = theme.keyTextColor, fontSize = 11.sp)
                        }
                    }

                    // Save / Apply
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(theme.actionKeyBackground)
                            .clickable {
                                onSaveOrder(workingOrder)
                                onClose()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("save_layout_order"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Valider",
                                tint = theme.actionKeyTextColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                "Appliquer",
                                color = theme.actionKeyTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Interactive Keys Map for reordering
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                rows.forEachIndexed { rowIndex, rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowKeys.forEach { keyLabel ->
                            val isSelected = selectedKeyForSwap == keyLabel
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) theme.actionKeyBackground else theme.keyBackground)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.dp, theme.suggestionTextColor, RoundedCornerShape(6.dp))
                                        } else {
                                            Modifier.border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                        }
                                    )
                                    .clickable {
                                        if (selectedKeyForSwap == null) {
                                            selectedKeyForSwap = keyLabel
                                        } else if (selectedKeyForSwap == keyLabel) {
                                            selectedKeyForSwap = null
                                        } else {
                                            swapKeys(selectedKeyForSwap!!, keyLabel)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = keyLabel.uppercase(),
                                    color = if (isSelected) theme.actionKeyTextColor else theme.keyTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
