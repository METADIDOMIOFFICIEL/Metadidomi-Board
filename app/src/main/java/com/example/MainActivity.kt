package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.MBoardPreferences
import com.example.data.SuggestionEngine
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SnippetsScreen
import com.example.ui.screens.ThemeScreen
import com.example.ui.theme.MBoardTheme

enum class MainTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Studio", Icons.Filled.Keyboard, "tab_home"),
    THEMES("Thèmes", Icons.Filled.Palette, "tab_themes"),
    SETTINGS("Préférences", Icons.Filled.Settings, "tab_settings"),
    SNIPPETS("Presse-papiers", Icons.Filled.ContentPaste, "tab_snippets"),
    HELP("Astuces", Icons.Filled.HelpOutline, "tab_help")
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: MBoardPreferences
    private lateinit var suggestionEngine: SuggestionEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = MBoardPreferences(this)
        suggestionEngine = SuggestionEngine(preferences, this)

        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 101)
        }

        setContent {
            MBoardTheme {
                MBoardApp(
                    preferences = preferences,
                    suggestionEngine = suggestionEngine
                )
            }
        }
    }
}

@Composable
fun MBoardApp(
    preferences: MBoardPreferences,
    suggestionEngine: SuggestionEngine
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = MainTab.values()

    // Handle back button to return to home screen
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tabs[selectedTab]) {
                MainTab.HOME -> {
                    HomeScreen(
                        preferences = preferences,
                        suggestionEngine = suggestionEngine,
                        onNavigateToThemes = { selectedTab = 1 }
                    )
                }
                MainTab.THEMES -> {
                    ThemeScreen(preferences = preferences)
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(preferences = preferences)
                }
                MainTab.SNIPPETS -> {
                    SnippetsScreen(preferences = preferences)
                }
                MainTab.HELP -> {
                    HelpScreen()
                }
            }
        }
    }
}
