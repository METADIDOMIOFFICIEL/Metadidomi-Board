package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MBoardPreferences
import com.example.data.SuggestionEngine
import com.example.R
import com.example.model.KeyboardThemes
import com.example.ui.MBoardKeyboardView

@Composable
fun HomeScreen(
    preferences: MBoardPreferences,
    suggestionEngine: SuggestionEngine,
    onNavigateToThemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isImeEnabled by remember { mutableStateOf(false) }
    var isImeSelected by remember { mutableStateOf(false) }
    var sandboxText by remember { mutableStateOf("") }
    var showEmbeddedKeyboard by remember { mutableStateOf(true) }
    var isFloatingState by remember { mutableStateOf(preferences.isFloating) }

    fun checkImeStatus() {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            val enabledMethods = imm?.enabledInputMethodList ?: emptyList()
            val packageName = context.packageName
            isImeEnabled = enabledMethods.any { it.packageName == packageName }

            val currentIme = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
            isImeSelected = currentIme?.contains(packageName) == true
        } catch (e: Exception) {
            isImeEnabled = false
            isImeSelected = false
        }
    }

    LaunchedEffect(Unit) {
        checkImeStatus()
    }

    // Root container allowing full-screen superposition when floating mode is active
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("home_header_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_mboard),
                            contentDescription = "Logo MBoard",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MBoard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Clavier moderne avec thèmes d'Afrique, vibrations haptiques & superposition",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Setup Wizard Card (Activation Flow)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isImeEnabled && isImeSelected)
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().testTag("activation_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isImeEnabled && isImeSelected) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (isImeEnabled && isImeSelected) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isImeEnabled && isImeSelected) "MBoard est actif et configuré !" else "Configuration du clavier MBoard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Suivez les 2 étapes simples pour utiliser MBoard dans toutes vos applications (SMS, WhatsApp, Chrome...) :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Step 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Étape 1 : Activer MBoard",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isImeEnabled) "Activé dans les paramètres du système" else "Dans Claviers virtuels > Gérer les claviers",
                                fontSize = 11.sp,
                                color = if (isImeEnabled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isImeEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("enable_ime_button")
                        ) {
                            Text(if (isImeEnabled) "Vérifié ✓" else "Activer")
                        }
                    }

                    // Step 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Étape 2 : Choisir MBoard par défaut",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isImeSelected) "MBoard est votre clavier par défaut" else "Sélectionnez MBoard dans la boîte de dialogue",
                                fontSize = 11.sp,
                                color = if (isImeSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Button(
                            onClick = {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isImeSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("select_ime_button")
                        ) {
                            Text(if (isImeSelected) "Sélectionné ✓" else "Choisir")
                        }
                    }
                }
            }

            // Live Test Sandbox
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("sandbox_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Bac à sable & Test en direct",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(sandboxText))
                                },
                                modifier = Modifier.size(32.dp).testTag("sandbox_copy_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copier le texte",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { sandboxText = "" },
                                modifier = Modifier.size(32.dp).testTag("sandbox_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Effacer le texte",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Testez la frappe, le retour tactile haptique, les suggestions prédictives, les thèmes et le mode superposition ci-dessous :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sandbox Text Field
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .padding(bottom = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            if (sandboxText.isEmpty()) {
                                Text(
                                    text = "Tapez sur les touches MBoard ci-dessous pour tester...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = sandboxText,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    // Floating mode indicator when active
                    if (isFloatingState) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OpenWith,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mode Superposition Actif",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Le clavier flotte librement au-dessus de tout l'écran. Déplacez-le n'importe où.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Button(
                                    onClick = {
                                        preferences.isFloating = false
                                        isFloatingState = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Ancrer", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Toggle for embedded simulator keyboard when docked
                    if (!isFloatingState) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEmbeddedKeyboard = !showEmbeddedKeyboard },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (showEmbeddedKeyboard) "Masquer le clavier interactif" else "Afficher le clavier interactif",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (showEmbeddedKeyboard) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Live Embedded Keyboard (Docked mode)
                        AnimatedVisibility(visible = showEmbeddedKeyboard) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            ) {
                                MBoardKeyboardView(
                                    preferences = preferences,
                                    suggestionEngine = suggestionEngine,
                                    currentInputText = sandboxText,
                                    onCommitText = { text ->
                                        sandboxText += text
                                    },
                                    onDelete = {
                                        if (sandboxText.isNotEmpty()) {
                                            val lastCodePoint = sandboxText.codePointBefore(sandboxText.length)
                                            val charCount = Character.charCount(lastCodePoint)
                                            sandboxText = sandboxText.dropLast(charCount)
                                        }
                                    },
                                    onEnter = {
                                        sandboxText += "\n"
                                    },
                                    onMoveCursor = { /* embedded preview cursor */ },
                                    onSelectAll = { /* select */ },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(sandboxText))
                                    },
                                    onCut = {
                                        clipboardManager.setText(AnnotatedString(sandboxText))
                                        sandboxText = ""
                                    },
                                    onPaste = {
                                        val clip = clipboardManager.getText()?.text
                                        if (!clip.isNullOrEmpty()) {
                                            sandboxText += clip
                                        }
                                    },
                                    onOpenSettings = onNavigateToThemes,
                                    onHideKeyboard = { showEmbeddedKeyboard = false },
                                    onFloatingToggle = { isFloating ->
                                        isFloatingState = isFloating
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Quick feature highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureHighlightBadge(
                    title = "Thème MBoard",
                    value = KeyboardThemes.getTheme(preferences.themeId).id.displayName,
                    onClick = onNavigateToThemes,
                    modifier = Modifier.weight(1f)
                )
                FeatureHighlightBadge(
                    title = "Disposition",
                    value = preferences.layoutType.displayName.substringBefore(" "),
                    onClick = onNavigateToThemes,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Full Screen Superposition Overlay when floating mode is active
        if (isFloatingState) {
            MBoardKeyboardView(
                preferences = preferences,
                suggestionEngine = suggestionEngine,
                currentInputText = sandboxText,
                onCommitText = { text ->
                    sandboxText += text
                },
                onDelete = {
                    if (sandboxText.isNotEmpty()) {
                        val lastCodePoint = sandboxText.codePointBefore(sandboxText.length)
                        val charCount = Character.charCount(lastCodePoint)
                        sandboxText = sandboxText.dropLast(charCount)
                    }
                },
                onEnter = {
                    sandboxText += "\n"
                },
                onMoveCursor = { /* cursor */ },
                onSelectAll = { /* select */ },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(sandboxText))
                },
                onCut = {
                    clipboardManager.setText(AnnotatedString(sandboxText))
                    sandboxText = ""
                },
                onPaste = {
                    val clip = clipboardManager.getText()?.text
                    if (!clip.isNullOrEmpty()) {
                        sandboxText += clip
                    }
                },
                onOpenSettings = onNavigateToThemes,
                onHideKeyboard = { isFloatingState = false },
                onFloatingToggle = { isFloating ->
                    isFloatingState = isFloating
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FeatureHighlightBadge(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
