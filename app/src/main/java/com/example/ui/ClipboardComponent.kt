package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem
import com.example.data.MBoardPreferences
import com.example.model.KeyboardThemeColors

@Composable
fun ClipboardView(
    theme: KeyboardThemeColors,
    preferences: MBoardPreferences,
    onClipSelected: (String) -> Unit,
    onBackToAlpha: () -> Unit,
    modifier: Modifier = Modifier
) {
    var clips by remember { mutableStateOf(preferences.getClipboardItems()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newSnippetText by remember { mutableStateOf("") }

    fun refreshClips() {
        clips = preferences.getClipboardItems()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(theme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Presse-papiers MBoard",
                color = theme.keyTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Add snippet button
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(32.dp).testTag("clipboard_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Ajouter une phrase",
                        tint = theme.suggestionTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Clear unpinned
                IconButton(
                    onClick = {
                        preferences.clearUnpinnedClipboard()
                        refreshClips()
                    },
                    modifier = Modifier.size(32.dp).testTag("clipboard_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Vider l'historique récent",
                        tint = theme.keySubTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // List of clips
        if (clips.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun texte copié pour l'instant.\nCopiez du texte ou ajoutez un snippet !",
                    color = theme.keySubTextColor,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(clips, key = { index, item -> "${item.id}_$index" }) { _, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.keyBackground)
                            .clickable { onClipSelected(item.text) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.text,
                                color = theme.keyTextColor,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Pin toggle
                        IconButton(
                            onClick = {
                                preferences.togglePin(item.id)
                                refreshClips()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (item.isPinned) "Détacher" else "Épingler",
                                tint = if (item.isPinned) theme.actionKeyBackground else theme.keySubTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete
                        IconButton(
                            onClick = {
                                preferences.deleteClipboardItem(item.id)
                                refreshClips()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Supprimer",
                                tint = theme.keySubTextColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom bar: ABC back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(theme.toolbarBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.specialKeyBackground)
                    .clickable { onBackToAlpha() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("clipboard_back_to_abc"),
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
        // Inline Add Snippet Panel
        if (showAddDialog) {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = theme.keyBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nouvelle phrase rapide :",
                        color = theme.keyTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newSnippetText,
                        onValueChange = { newSnippetText = it },
                        placeholder = {
                            Text("Ex: Mon adresse, mon email...", color = theme.keySubTextColor, fontSize = 12.sp)
                        },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.keyTextColor,
                            unfocusedTextColor = theme.keyTextColor,
                            focusedBorderColor = theme.suggestionTextColor,
                            unfocusedBorderColor = theme.keySubTextColor.copy(alpha = 0.3f),
                            focusedContainerColor = theme.background,
                            unfocusedContainerColor = theme.background
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Annuler", color = theme.keySubTextColor)
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                        Button(
                            onClick = {
                                if (newSnippetText.isNotBlank()) {
                                    preferences.addClipboardItem(newSnippetText, isPinned = true)
                                    refreshClips()
                                    newSnippetText = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.actionKeyBackground)
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }
}
