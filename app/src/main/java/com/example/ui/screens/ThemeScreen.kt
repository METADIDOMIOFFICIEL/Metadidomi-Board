package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.KeyHeightMode
import com.example.data.MBoardPreferences
import com.example.model.KeyboardThemeColors
import com.example.model.KeyboardThemeId
import com.example.model.KeyboardThemes
import com.example.ui.KeyboardBackgroundView

@Composable
fun ThemeScreen(
    preferences: MBoardPreferences,
    modifier: Modifier = Modifier
) {
    var selectedThemeId by remember { mutableStateOf(preferences.themeId) }
    var showBorders by remember { mutableStateOf(preferences.showKeyBorders) }
    var showNumbers by remember { mutableStateOf(preferences.showNumberRow) }
    var keyHeight by remember { mutableStateOf(preferences.keyHeight) }

    var bgImageUri by remember { mutableStateOf(preferences.backgroundImageUri) }
    var bgPreset by remember { mutableStateOf(preferences.backgroundImagePreset) }
    var bgDarkness by remember { mutableFloatStateOf(preferences.backgroundOverlayDarkness) }
    var showApplyConfirmation by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            bgImageUri = uri.toString()
            bgPreset = null
        }
    }

    // African National Themes (Cameroon by default)
    val africanThemesList = listOf(
        KeyboardThemes.Cameroon,
        KeyboardThemes.Senegal,
        KeyboardThemes.CoteDIvoire,
        KeyboardThemes.CongoDrc,
        KeyboardThemes.Nigeria,
        KeyboardThemes.Mali,
        KeyboardThemes.SouthAfrica,
        KeyboardThemes.Morocco
    )

    // Other modern/classic themes
    val classicThemesList = listOf(
        KeyboardThemes.GBoardBlue,
        KeyboardThemes.DarkAmoled,
        KeyboardThemes.CleanLight,
        KeyboardThemes.LavenderPastel,
        KeyboardThemes.MintEmerald,
        KeyboardThemes.NeonCyber,
        KeyboardThemes.SunsetCoral
    )

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
                imageVector = Icons.Filled.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Thèmes & Apparence",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Choisissez parmi les thèmes d'Afrique (Cameroun par défaut, Sénégal, Côte d'Ivoire, etc.), ou importez vos photos de fond.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Live Mini Preview Box
        val currentTheme = KeyboardThemes.getTheme(selectedThemeId)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().testTag("theme_live_preview")
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background preview
                if (!bgImageUri.isNullOrEmpty() || !bgPreset.isNullOrEmpty()) {
                    KeyboardBackgroundView(
                        imageUri = bgImageUri,
                        preset = bgPreset,
                        darkness = bgDarkness
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mini toolbar preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .background(currentTheme.toolbarBackground.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${currentTheme.id.displayName}",
                            color = currentTheme.suggestionTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Merci  D'accord",
                            color = currentTheme.keyTextColor,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (selectedThemeId == KeyboardThemeId.CAMEROON) "★" else "😊",
                            color = currentTheme.suggestionTextColor,
                            fontSize = 13.sp
                        )
                    }

                    // Mini Key row preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { char ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(currentTheme.keyBackground.copy(alpha = if (!bgImageUri.isNullOrEmpty() || !bgPreset.isNullOrEmpty()) 0.8f else 1f))
                                    .then(
                                        if (showBorders) {
                                            Modifier.border(
                                                0.5.dp,
                                                if (currentTheme.isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    color = currentTheme.keyTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Mini space row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(currentTheme.specialKeyBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("?123", color = currentTheme.specialKeyTextColor, fontSize = 10.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(4f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(currentTheme.keyBackground.copy(alpha = if (!bgImageUri.isNullOrEmpty() || !bgPreset.isNullOrEmpty()) 0.8f else 1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("espace", color = currentTheme.keyTextColor.copy(alpha = 0.6f), fontSize = 10.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(currentTheme.actionKeyBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedThemeId == KeyboardThemeId.CAMEROON) "★" else "↵",
                                color = currentTheme.actionKeyTextColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section 1: African Countries Themes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Public,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Thèmes des Pays d'Afrique 🌍",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            africanThemesList.forEach { themeItem ->
                val isSelected = themeItem.id == selectedThemeId
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedThemeId = themeItem.id
                        }
                        .testTag("theme_card_${themeItem.id.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Swatch previews matching country flag
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(themeItem.background))
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(themeItem.keyBackground))
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(themeItem.actionKeyBackground))
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = themeItem.id.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (themeItem.id == KeyboardThemeId.CAMEROON) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFFCD116))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Par défaut ★",
                                                color = Color(0xFF1E1E1E),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = themeItem.id.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Actif",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Classic / Modern Themes
        Text(
            text = "Thèmes Classiques & Dégradés 🎨",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            classicThemesList.forEach { themeItem ->
                val isSelected = themeItem.id == selectedThemeId
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedThemeId = themeItem.id
                        }
                        .testTag("theme_card_${themeItem.id.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(themeItem.background))
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(themeItem.keyBackground))
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(themeItem.actionKeyBackground))
                            }

                            Column {
                                Text(
                                    text = themeItem.id.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = themeItem.id.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Actif",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Custom background image section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("background_image_card")
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
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Image de fond du clavier",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Choisissez une photo de votre galerie ou un arrière-plan thématique pour votre clavier :",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Pick from gallery button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f).testTag("pick_photo_button")
                    ) {
                        Icon(imageVector = Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Galerie photos")
                    }

                    if (!bgImageUri.isNullOrEmpty() || !bgPreset.isNullOrEmpty()) {
                        OutlinedButton(
                            onClick = {
                                bgImageUri = null
                                bgPreset = null
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Supprimer")
                        }
                    }
                }

                // Preset artistic backgrounds
                Text(text = "Ou choisissez un fond artistique prédéfini :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple("cameroon", "🇨🇲 Cameroun", Brush.horizontalGradient(listOf(Color(0xFF007A3D), Color(0xFFCE1126), Color(0xFFFCD116)))),
                        Triple("senegal", "🇸🇳 Sénégal", Brush.horizontalGradient(listOf(Color(0xFF00853F), Color(0xFFFDEF42), Color(0xFFE31B23)))),
                        Triple("cotedivoire", "🇨🇮 Côte d'Ivoire", Brush.horizontalGradient(listOf(Color(0xFFFF8200), Color(0xFFFFFFFF), Color(0xFF009A44)))),
                        Triple("aurora", "🌌 Aurore", Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))),
                        Triple("cyber", "💜 Cyber", Brush.linearGradient(listOf(Color(0xFF0D0221), Color(0xFF241734), Color(0xFF0F084B)))),
                        Triple("sunset", "🌅 Sunset", Brush.verticalGradient(listOf(Color(0xFF4A154B), Color(0xFF7E1C52), Color(0xFFC75146))))
                    )

                    presets.forEach { (key, label, brush) ->
                        val isChosen = bgPreset == key && bgImageUri.isNullOrEmpty()
                        Box(
                            modifier = Modifier
                                .size(width = 100.dp, height = 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(brush)
                                .then(
                                    if (isChosen) Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)) else Modifier
                                )
                                .clickable {
                                    bgPreset = key
                                    bgImageUri = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Darkness slider for legibility
                if (!bgImageUri.isNullOrEmpty() || !bgPreset.isNullOrEmpty()) {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Assombrir l'image (lisibilité) : ${(bgDarkness * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Slider(
                            value = bgDarkness,
                            onValueChange = {
                                bgDarkness = it
                            },
                            valueRange = 0.0f..0.85f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }
        }

        // Appearance Adjustments Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Options d'affichage",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Key Borders Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Contours des touches", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Affiche une bordure distincte autour de chaque touche",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showBorders,
                        onCheckedChange = {
                            showBorders = it
                        },
                        modifier = Modifier.testTag("toggle_key_borders")
                    )
                }

                // Number row switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Ligne de chiffres dédiée", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            text = "Affiche la rangée numérique 1-0 au-dessus des lettres",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showNumbers,
                        onCheckedChange = {
                            showNumbers = it
                        },
                        modifier = Modifier.testTag("toggle_number_row")
                    )
                }

                // Key Height selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Hauteur des touches", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeyHeightMode.values().forEach { mode ->
                            val isChosen = keyHeight == mode
                            FilterChip(
                                selected = isChosen,
                                onClick = {
                                    keyHeight = mode
                                },
                                label = { Text(mode.displayName) },
                                modifier = Modifier.weight(1f).testTag("height_${mode.name}")
                            )
                        }
                    }
                }

                Button(
                    onClick = { showApplyConfirmation = true },
                    modifier = Modifier.fillMaxWidth().testTag("apply_theme_settings_button")
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Appliquer les modifications")
                }
            }

            if (showApplyConfirmation) {
                AlertDialog(
                    onDismissRequest = { showApplyConfirmation = false },
                    title = { Text("Appliquer l’apparence ?") },
                    text = { Text("Le thème, le fond et les options d’affichage seront enregistrés après confirmation.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                preferences.themeId = selectedThemeId
                                preferences.backgroundImageUri = bgImageUri
                                preferences.backgroundImagePreset = bgPreset
                                preferences.backgroundOverlayDarkness = bgDarkness
                                preferences.showKeyBorders = showBorders
                                preferences.showNumberRow = showNumbers
                                preferences.keyHeight = keyHeight
                                showApplyConfirmation = false
                            },
                            modifier = Modifier.testTag("confirm_apply_theme_settings")
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
        }
    }
}
