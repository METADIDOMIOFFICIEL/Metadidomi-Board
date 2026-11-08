package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

@Composable
fun HelpScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = remember(context) {
        val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName ?: "—"
    }
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
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Astuces & Guide MBoard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Découvrez toutes les fonctionnalités avancées de votre clavier MBoard pour taper plus vite et plus confortablement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tips List
        TipCard(
            title = "Accès rapide aux accents français",
            description = "Maintenez le doigt enfoncé sur n'importe quelle lettre (par exemple 'e', 'a', 'c') pour ouvrir le sélecteur d'accents instantané (é, è, ê, ë, à, ç, €...).",
            icon = Icons.Filled.TextFields
        )

        TipCard(
            title = "Barre d'outils MBoard (Touche 'M')",
            description = "Appuyez sur le logo 'M' à gauche de la barre de suggestions pour déployer la rangée d'outils rapides : émoticônes, presse-papiers, sélecteur de langue, thèmes et curseur.",
            icon = Icons.Filled.AutoAwesome
        )

        TipCard(
            title = "Navigation précise du curseur",
            description = "Ouvrez le mode édition de texte depuis la barre d'outils pour naviguer caractère par caractère ou ligne par ligne grâce au pavé directionnel.",
            icon = Icons.Filled.OpenWith
        )

        TipCard(
            title = "Phrases rapides & Presse-papiers",
            description = "Épinglez vos adresses, messages types ou coordonnées dans le presse-papiers MBoard pour les coller d'un simple toucher sans devoir les retaper.",
            icon = Icons.Filled.HelpOutline
        )

        TipCard(
            title = "Dictée vocale fluide",
            description = "Utilisez le bouton micro pour transcrire instantanément vos paroles en texte sans quitter votre application.",
            icon = Icons.Filled.Mic
        )

        TipCard(
            title = "Thèmes modernes & AMOLED",
            description = "Basculez entre le thème bleu MBoard, le noir absolu AMOLED pour préserver votre batterie, ou nos thèmes pastels et cyberpunk.",
            icon = Icons.Filled.Palette
        )

        Text(
            text = "Version $appVersion",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "© 2026 METADIDOMI — Tous droits réservés.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "https://metadidomi-global.com/",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { uriHandler.openUri("https://metadidomi-global.com/") }
        )
    }
}

@Composable
private fun TipCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
