package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MBoardBlueLight,
    onPrimary = Color(0xFF002D6E),
    primaryContainer = Color(0xFF174EA6),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = MBoardSecondaryLight,
    onSecondary = Color(0xFF003730),
    background = MBoardDarkBackground,
    surface = MBoardDarkSurface,
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFE8EAED)
)

private val LightColorScheme = lightColorScheme(
    primary = MBoardBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = MBoardSecondary,
    onSecondary = Color.White,
    background = MBoardLightBackground,
    surface = MBoardLightSurface,
    onBackground = Color(0xFF202124),
    onSurface = Color(0xFF202124)
)

@Composable
fun MBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    MBoardTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
