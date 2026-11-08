package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardThemeColors

@Composable
fun TextEditingPadView(
    theme: KeyboardThemeColors,
    onMoveCursor: (direction: Int) -> Unit, // -1 for left, +1 for right, -100 for line up, +100 for line down
    onJumpStart: () -> Unit,
    onJumpEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onBackToAlpha: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(theme.background)
            .padding(8.dp)
    ) {
        // Top action bar: Select All, Cut, Copy, Paste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditActionButton(
                label = "Tout",
                icon = Icons.Filled.SelectAll,
                theme = theme,
                onClick = onSelectAll,
                modifier = Modifier.weight(1f),
                testTag = "edit_select_all"
            )
            EditActionButton(
                label = "Couper",
                icon = Icons.Filled.ContentCut,
                theme = theme,
                onClick = onCut,
                modifier = Modifier.weight(1f),
                testTag = "edit_cut"
            )
            EditActionButton(
                label = "Copier",
                icon = Icons.Filled.ContentCopy,
                theme = theme,
                onClick = onCopy,
                modifier = Modifier.weight(1f),
                testTag = "edit_copy"
            )
            EditActionButton(
                label = "Coller",
                icon = Icons.Filled.ContentPaste,
                theme = theme,
                onClick = onPaste,
                modifier = Modifier.weight(1f),
                testTag = "edit_paste"
            )
        }

        // Center D-Pad Navigation Area
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Jump to start (Début)
            DPadButton(
                icon = Icons.Filled.FirstPage,
                contentDesc = "Début du texte",
                theme = theme,
                onClick = onJumpStart,
                testTag = "dpad_home"
            )

            // D-Pad Cross
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // UP
                DPadButton(
                    icon = Icons.Filled.ArrowUpward,
                    contentDesc = "Haut",
                    theme = theme,
                    onClick = { onMoveCursor(-100) },
                    testTag = "dpad_up"
                )

                // LEFT, CENTER, RIGHT
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DPadButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDesc = "Gauche",
                        theme = theme,
                        onClick = { onMoveCursor(-1) },
                        testTag = "dpad_left"
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.specialKeyBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⇄",
                            color = theme.specialKeyTextColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DPadButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDesc = "Droite",
                        theme = theme,
                        onClick = { onMoveCursor(1) },
                        testTag = "dpad_right"
                    )
                }

                // DOWN
                DPadButton(
                    icon = Icons.Filled.ArrowDownward,
                    contentDesc = "Bas",
                    theme = theme,
                    onClick = { onMoveCursor(100) },
                    testTag = "dpad_down"
                )
            }

            // Jump to end (Fin)
            DPadButton(
                icon = Icons.Filled.LastPage,
                contentDesc = "Fin du texte",
                theme = theme,
                onClick = onJumpEnd,
                testTag = "dpad_end"
            )
        }

        // Bottom back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.specialKeyBackground)
                    .clickable { onBackToAlpha() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("textedit_back_to_abc"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Retour au clavier",
                    color = theme.specialKeyTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun EditActionButton(
    label: String,
    icon: ImageVector,
    theme: KeyboardThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(theme.keyBackground)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = theme.suggestionTextColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = theme.keyTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    contentDesc: String,
    theme: KeyboardThemeColors,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(theme.actionKeyBackground)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = theme.actionKeyTextColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
