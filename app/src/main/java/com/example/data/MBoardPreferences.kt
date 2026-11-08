package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.KeyboardLayoutType
import com.example.model.KeyboardThemeId
import org.json.JSONArray
import org.json.JSONObject

import java.util.UUID

data class ClipboardItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

enum class KeyHeightMode(val displayName: String, val multiplier: Float) {
    COMPACT("Compact", 0.88f),
    NORMAL("Normal", 1.0f),
    TALL("Grand", 1.15f)
}

class MBoardPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mboard_settings", Context.MODE_PRIVATE)

    var themeId: KeyboardThemeId
        get() {
            val name = prefs.getString("theme_id", KeyboardThemeId.CAMEROON.name) ?: KeyboardThemeId.CAMEROON.name
            return try { KeyboardThemeId.valueOf(name) } catch (e: Exception) { KeyboardThemeId.CAMEROON }
        }
        set(value) = prefs.edit().putString("theme_id", value.name).apply()

    var layoutType: KeyboardLayoutType
        get() {
            val name = prefs.getString("layout_type", KeyboardLayoutType.AZERTY.name) ?: KeyboardLayoutType.AZERTY.name
            return try { KeyboardLayoutType.valueOf(name) } catch (e: Exception) { KeyboardLayoutType.AZERTY }
        }
        set(value) = prefs.edit().putString("layout_type", value.name).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean("show_number_row", true)
        set(value) = prefs.edit().putBoolean("show_number_row", value).apply()

    var keyPopupEnabled: Boolean
        get() = prefs.getBoolean("key_popup_enabled", true)
        set(value) = prefs.edit().putBoolean("key_popup_enabled", value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean("haptic_feedback", true)
        set(value) = prefs.edit().putBoolean("haptic_feedback", value).apply()

    var hapticStrength: Float
        get() = prefs.getFloat("haptic_strength", 0.5f)
        set(value) = prefs.edit().putFloat("haptic_strength", value).apply()

    var soundFeedback: Boolean
        get() = prefs.getBoolean("sound_feedback", false)
        set(value) = prefs.edit().putBoolean("sound_feedback", value).apply()

    var soundVolume: Float
        get() = prefs.getFloat("sound_volume", 0.6f)
        set(value) = prefs.edit().putFloat("sound_volume", value).apply()

    var autoCapitalization: Boolean
        get() = prefs.getBoolean("auto_capitalization", true)
        set(value) = prefs.edit().putBoolean("auto_capitalization", value).apply()

    var keyHeight: KeyHeightMode
        get() {
            val name = prefs.getString("key_height", KeyHeightMode.NORMAL.name) ?: KeyHeightMode.NORMAL.name
            return try { KeyHeightMode.valueOf(name) } catch (e: Exception) { KeyHeightMode.NORMAL }
        }
        set(value) = prefs.edit().putString("key_height", value.name).apply()

    var showKeyBorders: Boolean
        get() = prefs.getBoolean("show_key_borders", true)
        set(value) = prefs.edit().putBoolean("show_key_borders", value).apply()

    // Font Styles (like Fonts Keyboard)
    var activeFontStyle: com.example.model.FontStyle
        get() {
            val id = prefs.getString("active_font_style", com.example.model.FontStyle.NORMAL.name)
            return try {
                com.example.model.FontStyle.valueOf(id ?: com.example.model.FontStyle.NORMAL.name)
            } catch (e: Exception) {
                com.example.model.FontStyle.NORMAL
            }
        }
        set(value) = prefs.edit().putString("active_font_style", value.name).apply()

    // Floating Mode (like Gboard)
    var isFloating: Boolean
        get() = prefs.getBoolean("is_floating", false)
        set(value) = prefs.edit().putBoolean("is_floating", value).apply()

    var floatingScale: Float
        get() = prefs.getFloat("floating_scale", 0.88f)
        set(value) = prefs.edit().putFloat("floating_scale", value).apply()

    var floatingOpacity: Float
        get() = prefs.getFloat("floating_opacity", 0.95f)
        set(value) = prefs.edit().putFloat("floating_opacity", value).apply()

    // Keyboard Resizing (like Gboard)
    var keyboardScale: Float
        get() = prefs.getFloat("keyboard_scale", 1.0f)
        set(value) = prefs.edit().putFloat("keyboard_scale", value).apply()

    var keyboardWidthRatio: Float
        get() = prefs.getFloat("keyboard_width_ratio", 1.0f)
        set(value) = prefs.edit().putFloat("keyboard_width_ratio", value).apply()

    var keyboardBottomOffset: Int
        get() = prefs.getInt("keyboard_bottom_offset", 0)
        set(value) = prefs.edit().putInt("keyboard_bottom_offset", value).apply()

    // Typing assistance
    var autoCorrection: Boolean
        get() = prefs.getBoolean("auto_correction", true)
        set(value) = prefs.edit().putBoolean("auto_correction", value).apply()

    var spellingUnderlineEnabled: Boolean
        get() = prefs.getBoolean("spelling_underline_enabled", true)
        set(value) = prefs.edit().putBoolean("spelling_underline_enabled", value).apply()

    var grammarCheckingEnabled: Boolean
        get() = prefs.getBoolean("grammar_checking_enabled", false)
        set(value) = prefs.edit().putBoolean("grammar_checking_enabled", value).apply()

    var nextWordPrediction: Boolean
        get() = prefs.getBoolean("next_word_prediction", true)
        set(value) = prefs.edit().putBoolean("next_word_prediction", value).apply()

    // Font Style Color Selection
    var activeFontColorHex: String
        get() = prefs.getString("active_font_color_hex", "#FFFFFF") ?: "#FFFFFF"
        set(value) = prefs.edit().putString("active_font_color_hex", value).apply()

    // Free Floating Position
    var floatingPositionX: Float
        get() = prefs.getFloat("floating_pos_x", 0f)
        set(value) = prefs.edit().putFloat("floating_pos_x", value).apply()

    var floatingPositionY: Float
        get() = prefs.getFloat("floating_pos_y", 0f)
        set(value) = prefs.edit().putFloat("floating_pos_y", value).apply()

    // Custom Background Image (like Gboard)
    var backgroundImageUri: String?
        get() = prefs.getString("background_image_uri", null)
        set(value) = prefs.edit().putString("background_image_uri", value).apply()

    var backgroundImagePreset: String?
        get() = prefs.getString("background_image_preset", null)
        set(value) = prefs.edit().putString("background_image_preset", value).apply()

    var backgroundOverlayDarkness: Float
        get() = prefs.getFloat("background_overlay_darkness", 0.40f)
        set(value) = prefs.edit().putFloat("background_overlay_darkness", value).apply()

    // Key Reorganization / Custom Key Positions
    var customAlphaKeys: String?
        get() = prefs.getString("custom_alpha_keys", null)
        set(value) = prefs.edit().putString("custom_alpha_keys", value).apply()

    // Clipboard & Snippets
    fun getClipboardItems(): List<ClipboardItem> {
        val raw = prefs.getString("clipboard_history", null) ?: return getDefaultSnippets()
        val list = mutableListOf<ClipboardItem>()
        val seenIds = mutableSetOf<String>()
        var hadDuplicates = false
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                var id = obj.optString("id")
                if (id.isNullOrEmpty() || seenIds.contains(id)) {
                    id = UUID.randomUUID().toString()
                    hadDuplicates = true
                }
                seenIds.add(id)
                list.add(
                    ClipboardItem(
                        id = id,
                        text = obj.getString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
            if (hadDuplicates) {
                saveClipboardItems(list)
            }
        } catch (e: Exception) {
            return getDefaultSnippets()
        }
        return list
    }

    fun saveClipboardItems(items: List<ClipboardItem>) {
        val array = JSONArray()
        items.take(30).forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("timestamp", item.timestamp)
            obj.put("isPinned", item.isPinned)
            array.put(obj)
        }
        prefs.edit().putString("clipboard_history", array.toString()).apply()
    }

    fun addClipboardItem(text: String, isPinned: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val items = getClipboardItems().toMutableList()
        items.removeAll { it.text == trimmed }
        items.add(0, ClipboardItem(text = trimmed, isPinned = isPinned))
        saveClipboardItems(items)
    }

    fun togglePin(id: String) {
        val items = getClipboardItems().map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveClipboardItems(items)
    }

    fun deleteClipboardItem(id: String) {
        val items = getClipboardItems().filterNot { it.id == id }
        saveClipboardItems(items)
    }

    fun clearUnpinnedClipboard() {
        val items = getClipboardItems().filter { it.isPinned }
        saveClipboardItems(items)
    }

    private fun getDefaultSnippets(): List<ClipboardItem> {
        val defaults = listOf(
            ClipboardItem(id = "default_snippet_1", text = "Bonjour !", isPinned = true),
            ClipboardItem(id = "default_snippet_2", text = "Merci beaucoup !", isPinned = true),
            ClipboardItem(id = "default_snippet_3", text = "Je suis en route, j'arrive dans 10 minutes.", isPinned = true),
            ClipboardItem(id = "default_snippet_4", text = "Cordialement,", isPinned = true)
        )
        saveClipboardItems(defaults)
        return defaults
    }

    // Learned words
    fun getLearnedWords(): Set<String> {
        return prefs.getStringSet("learned_words", emptySet()) ?: emptySet()
    }

    fun addLearnedWord(word: String) {
        val clean = word.trim().lowercase()
        if (clean.length < 2) return
        val current = getLearnedWords().toMutableSet()
        current.add(clean)
        prefs.edit().putStringSet("learned_words", current).apply()
    }
}
