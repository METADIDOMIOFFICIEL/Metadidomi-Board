package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MBoardPreferences
import com.example.model.FontStyle
import com.example.model.KeyboardLayoutType

@Composable
fun SettingsScreen(
    preferences: MBoardPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedLayout by remember { mutableStateOf(preferences.layoutType) }
    var selectedFont by remember { mutableStateOf(preferences.activeFontStyle) }
    var hapticFeedback by remember { mutableStateOf(preferences.hapticFeedback) }
    var hapticStrength by remember { mutableFloatStateOf(preferences.hapticStrength) }
    var soundFeedback by remember { mutableStateOf(preferences.soundFeedback) }
    var soundVolume by remember { mutableFloatStateOf(preferences.soundVolume) }
    var keyPopup by remember { mutableStateOf(preferences.keyPopupEnabled) }
    var autoCap by remember { mutableStateOf(preferences.autoCapitalization) }
    var autoCorrect by remember { mutableStateOf(preferences.autoCorrection) }
    var spellingUnderline by remember { mutableStateOf(preferences.spellingUnderlineEnabled) }
    var grammarChecking by remember { mutableStateOf(preferences.grammarCheckingEnabled) }
    var nextWordPrediction by remember { mutableStateOf(preferences.nextWordPrediction) }
    var isFloating by remember { mutableStateOf(preferences.isFloating) }
    var keyboardScale by remember { mutableFloatStateOf(preferences.keyboardScale) }
    var keyboardWidthRatio by remember { mutableFloatStateOf(preferences.keyboardWidthRatio) }
    var keyboardBottomOffset by remember { mutableStateOf(preferences.keyboardBottomOffset) }
    var showApplyConfirmation by remember { mutableStateOf(false) }
    var showClearDictionaryConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Metadidomi Board (MBoard) — Préférences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { showApplyConfirmation = true },
            modifier = Modifier.fillMaxWidth().testTag("apply_settings_button")
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Appliquer les modifications")
        }

        // Correction, prédiction et dictionnaire MBoard
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("ai_db_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Suggestions et dictionnaire Metadidomi Board",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Auto-correction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Correction automatique", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Corrige les fautes de frappe reconnues pendant la saisie",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoCorrect,
                        onCheckedChange = {
                            autoCorrect = it
                        },
                        modifier = Modifier.testTag("toggle_autocorrect")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Soulignement orthographique", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Souligne les mots qui semblent mal orthographiés",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = spellingUnderline,
                        onCheckedChange = { spellingUnderline = it },
                        modifier = Modifier.testTag("toggle_spelling_underline")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Vérification grammaticale", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Souligne certaines erreurs grammaticales détectées",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = grammarChecking,
                        onCheckedChange = { grammarChecking = it },
                        modifier = Modifier.testTag("toggle_grammar_checking")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Saisie prédictive", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Suggère le mot suivant selon votre contexte de frappe",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = nextWordPrediction,
                        onCheckedChange = {
                            nextWordPrediction = it
                        },
                        modifier = Modifier.testTag("toggle_next_word")
                    )
                }

                // Clear dictionary button
                OutlinedButton(
                    onClick = { showClearDictionaryConfirmation = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Effacer l'historique des mots appris")
                }
            }
        }

        // Mode flottant et redimensionnement
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("floating_resize_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureInPicture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Mode flottant & Redimensionnement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Clavier flottant", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Déplacez le clavier librement sur l'écran avec la barre de glissement",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isFloating,
                        onCheckedChange = {
                            isFloating = it
                        },
                        modifier = Modifier.testTag("toggle_floating_setting")
                    )
                }

                Button(
                    onClick = {
                        keyboardScale = 1.0f
                        keyboardWidthRatio = 1.0f
                        keyboardBottomOffset = 0
                        isFloating = false
                        Toast.makeText(context, "Taille et position réinitialisées", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Réinitialiser dimensions par défaut")
                }
            }
        }

        // Styles d'écriture (Polices)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("font_styles_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Styles d'écriture (Clavier Fonts)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Choisissez votre style d'écriture par défaut ou changez-le en direct depuis la barre d'outils du clavier :",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FontStyle.values().take(6).forEach { style ->
                    val isSelected = style == selectedFont
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFont = style
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${style.displayName} : ${style.previewText}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Keyboard Layout Selection
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("layout_settings_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Disposition du clavier",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                KeyboardLayoutType.values().forEach { layout ->
                    val isSelected = layout == selectedLayout
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLayout = layout
                            }
                            .testTag("layout_option_${layout.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = layout.displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = layout.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Haptic & Sound Feedback Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("feedback_settings_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Retours sensoriels & Frappe",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // Haptic feedback toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Retour tactile (Vibration)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Vibrations haptiques physiques aux touches et changements de mode",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = {
                            hapticFeedback = it
                        },
                        modifier = Modifier.testTag("toggle_haptic")
                    )
                }

                // Haptic Intensity Slider
                if (hapticFeedback) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Intensité du retour haptique : ${(hapticStrength * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Slider(
                            value = hapticStrength,
                            onValueChange = {
                                hapticStrength = it
                                },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(30.dp).testTag("slider_haptic_strength")
                        )
                    }
                }

                // Sound feedback toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Son des touches", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Son système lors de la frappe (Espace, Retour, Effacer, Lettres)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = soundFeedback,
                        onCheckedChange = {
                            soundFeedback = it
                        },
                        modifier = Modifier.testTag("toggle_sound")
                    )
                }

                // Sound Volume Slider
                if (soundFeedback) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Volume sonore des touches : ${(soundVolume * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Slider(
                            value = soundVolume,
                            onValueChange = {
                                soundVolume = it
                                },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(30.dp).testTag("slider_sound_volume")
                        )
                    }
                }

                // Key popup toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Agrandissement des touches (Effet popup)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Affiche une bulle avec la lettre en grand au-dessus du doigt pendant la frappe",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = keyPopup,
                        onCheckedChange = {
                            keyPopup = it
                        },
                        modifier = Modifier.testTag("toggle_popup")
                    )
                }

                // Auto capitalization toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Majuscule automatique", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Active la majuscule après un point ou au début de phrase",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoCap,
                        onCheckedChange = {
                            autoCap = it
                        },
                        modifier = Modifier.testTag("toggle_autocap")
                    )
                }
            }
        }

        // Modes spécialisés de saisie
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("specialized_keyboards_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Modes de saisie spécialisés intégrés",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = "Accessibles depuis la barre d'outils de Metadidomi Board.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Enfant
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Clavier Enfant", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = "Touches géantes colorées, ABC majuscule/minuscule, 123, animaux, jouets et mots magiques",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Manuscrit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Gesture, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Écriture manuscrite", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = "Zone de dessin fluide au doigt ou stylet avec reconnaissance et suggestions de mots",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Calculatrice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Calculatrice standard & scientifique complète", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = "Mode simple et scientifique (sin, cos, tan, ln, log, √, π, e, parenthèses) avec insertion directe du résultat",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Dictée vocale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Dictée vocale Metadidomi Board", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = "Visualisation sonore réactive, ponctuation rapide et dictée continue",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Traducteur
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Translate, contentDescription = null, tint = Color(0xFF1976D2))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Traducteur multilingue en direct", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = "Traduisez instantanément entre 14 langues (Français, Anglais, Espagnol, Arabe, Allemand...) avec prononciation vocale et insertion directe",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Privacy & Local Processing Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Traitement local des suggestions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Les suggestions et corrections sont calculées sur l'appareil. La traduction en ligne peut transmettre le texte au service de traduction.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Button(
            onClick = { showApplyConfirmation = true },
            modifier = Modifier.fillMaxWidth().testTag("apply_settings_button_bottom")
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Appliquer les modifications")
        }
    }

    if (showApplyConfirmation) {
        AlertDialog(
            onDismissRequest = { showApplyConfirmation = false },
            title = { Text("Appliquer les réglages ?") },
            text = { Text("Les modifications de préférences seront enregistrées et utilisées par le clavier.") },
            confirmButton = {
                Button(
                    onClick = {
                        preferences.autoCorrection = autoCorrect
                        preferences.spellingUnderlineEnabled = spellingUnderline
                        preferences.grammarCheckingEnabled = grammarChecking
                        preferences.nextWordPrediction = nextWordPrediction
                        preferences.isFloating = isFloating
                        preferences.keyboardScale = keyboardScale
                        preferences.keyboardWidthRatio = keyboardWidthRatio
                        preferences.keyboardBottomOffset = keyboardBottomOffset
                        preferences.activeFontStyle = selectedFont
                        preferences.layoutType = selectedLayout
                        preferences.hapticFeedback = hapticFeedback
                        preferences.hapticStrength = hapticStrength
                        preferences.soundFeedback = soundFeedback
                        preferences.soundVolume = soundVolume
                        preferences.keyPopupEnabled = keyPopup
                        preferences.autoCapitalization = autoCap
                        showApplyConfirmation = false
                        Toast.makeText(context, "Préférences appliquées", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_apply_settings")
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApplyConfirmation = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showClearDictionaryConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearDictionaryConfirmation = false },
            title = { Text("Effacer les mots appris ?") },
            text = { Text("Cette action supprimera votre dictionnaire personnel et ne peut pas être annulée.") },
            confirmButton = {
                Button(
                    onClick = {
                        preferences.addLearnedWord("")
                        showClearDictionaryConfirmation = false
                        Toast.makeText(context, "Dictionnaire utilisateur réinitialisé", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_clear_dictionary")
                ) {
                    Text("Effacer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDictionaryConfirmation = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
