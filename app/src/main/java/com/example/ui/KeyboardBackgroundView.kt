package com.example.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun KeyboardBackgroundView(
    imageUri: String?,
    preset: String?,
    darkness: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            !imageUri.isNullOrEmpty() -> {
                AsyncImage(
                    model = Uri.parse(imageUri),
                    contentDescription = "Image de fond clavier",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            preset == "cameroon" -> {
                // Beautiful artistic Cameroon flag tricolor gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF007A3D), // Vert
                                    Color(0xFFCE1126), // Rouge
                                    Color(0xFFFCD116)  // Jaune
                                )
                            )
                        )
                )
            }
            preset == "aurora" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F2027),
                                    Color(0xFF203A43),
                                    Color(0xFF2C5364)
                                )
                            )
                        )
                )
            }
            preset == "cyber" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0D0221),
                                    Color(0xFF241734),
                                    Color(0xFF0F084B)
                                )
                            )
                        )
                )
            }
            preset == "sunset" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4A154B),
                                    Color(0xFF7E1C52),
                                    Color(0xFFC75146)
                                )
                            )
                        )
                )
            }
        }

        // Overlay darkness for legibility (comme dans Gboard)
        if (!imageUri.isNullOrEmpty() || !preset.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = darkness))
            )
        }
    }
}
