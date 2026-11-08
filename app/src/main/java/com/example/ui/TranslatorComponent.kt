package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MBoardPreferences
import com.example.ime.MBoardHaptics
import com.example.model.KeyboardThemeColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class TranslateLanguage(
    val code: String,
    val name: String,
    val flag: String,
    val locale: Locale
)

val SUPPORTED_TRANSLATE_LANGUAGES = listOf(
    TranslateLanguage("fr", "Français", "🇫🇷", Locale.FRENCH),
    TranslateLanguage("en", "Anglais", "🇬🇧", Locale.ENGLISH),
    TranslateLanguage("es", "Espagnol", "🇪🇸", Locale("es")),
    TranslateLanguage("de", "Allemand", "🇩🇪", Locale.GERMAN),
    TranslateLanguage("it", "Italien", "🇮🇹", Locale.ITALIAN),
    TranslateLanguage("pt", "Portugais", "🇵🇹", Locale("pt")),
    TranslateLanguage("ar", "Arabe", "🇸🇦", Locale("ar")),
    TranslateLanguage("zh", "Chinois", "🇨🇳", Locale.CHINESE),
    TranslateLanguage("ja", "Japonais", "🇯🇵", Locale.JAPANESE),
    TranslateLanguage("ru", "Russe", "🇷🇺", Locale("ru")),
    TranslateLanguage("sw", "Swahili", "🇰🇪", Locale("sw")),
    TranslateLanguage("ha", "Haoussa", "🇳🇬", Locale("ha")),
    TranslateLanguage("nl", "Néerlandais", "🇳🇱", Locale("nl")),
    TranslateLanguage("tr", "Turc", "🇹🇷", Locale("tr"))
)

@Composable
fun TranslatorView(
    theme: KeyboardThemeColors,
    preferences: MBoardPreferences,
    sourceText: String,
    onSourceTextChange: (String) -> Unit,
    onCommitText: (String) -> Unit,
    onBackToAlpha: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var sourceLanguage by remember { mutableStateOf(SUPPORTED_TRANSLATE_LANGUAGES[0]) } // Français
    var targetLanguage by remember { mutableStateOf(SUPPORTED_TRANSLATE_LANGUAGES[1]) } // Anglais
    var translatedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSwappedAnimation by remember { mutableFloatStateOf(0f) }

    var openLanguageMenu by remember { mutableStateOf<String?>(null) }

    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }

    // Initialize TextToSpeech safely
    DisposableEffect(Unit) {
        var ttsInstance: TextToSpeech? = null
        try {
            ttsInstance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech = ttsInstance
                }
            }
        } catch (e: Exception) { /* ignore */ }

        onDispose {
            try {
                ttsInstance?.stop()
                ttsInstance?.shutdown()
            } catch (e: Exception) { /* ignore */ }
        }
    }

    fun playFeedback() {
        if (preferences.soundFeedback) {
            try {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                am?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, preferences.soundVolume)
            } catch (e: Exception) { /* ignore */ }
        }
        if (preferences.hapticFeedback) {
            MBoardHaptics.performKeyClick(context, true, preferences.hapticStrength)
        }
    }

    // Debounce translation when source text changes
    LaunchedEffect(sourceText, sourceLanguage, targetLanguage) {
        if (sourceText.isBlank()) {
            translatedText = ""
            errorMessage = null
            isLoading = false
            return@LaunchedEffect
        }

        delay(350)
        isLoading = true
        errorMessage = null
        try {
            translatedText = withContext(Dispatchers.IO) {
                performTranslation(
                    query = sourceText.trim(),
                    sourceLang = sourceLanguage.code,
                    targetLang = targetLanguage.code
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            translatedText = getOfflineTranslation(sourceText.trim(), sourceLanguage.code, targetLanguage.code).orEmpty()
            if (translatedText.isEmpty()) errorMessage = "Erreur de connexion"
        } finally {
            isLoading = false
        }
    }

    val calculatedHeight = remember(preferences.keyHeight, preferences.keyboardScale, compact) {
        if (compact) {
            (180f * preferences.keyHeight.multiplier * preferences.keyboardScale).dp.coerceIn(160.dp, 230.dp)
        } else {
            (275f * preferences.keyHeight.multiplier * preferences.keyboardScale).dp.coerceIn(250.dp, 400.dp)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(calculatedHeight),
        color = theme.background
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            // Header Bar: Language Selectors ⇄ Return to keyboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.toolbarBackground)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Return button
                IconButton(
                    onClick = {
                        playFeedback()
                        onBackToAlpha()
                    },
                    modifier = Modifier.size(30.dp).testTag("translator_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "Retour au clavier",
                        tint = theme.keyTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Language Switcher Pair
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Source Language Selector
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.keyBackground)
                                .clickable { openLanguageMenu = if (openLanguageMenu == "source") null else "source" }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(sourceLanguage.flag, fontSize = 12.sp)
                            Text(
                                text = sourceLanguage.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.keyTextColor
                            )
                        }

                    }

                    // Swap Languages Button ⇄
                    val rotation by animateFloatAsState(targetValue = isSwappedAnimation, label = "swap_rot")
                    IconButton(
                        onClick = {
                            val temp = sourceLanguage
                            sourceLanguage = targetLanguage
                            targetLanguage = temp
                            val tempText = sourceText
                            onSourceTextChange(translatedText)
                            translatedText = tempText
                            isSwappedAnimation += 180f
                            playFeedback()
                        },
                        modifier = Modifier.size(28.dp).rotate(rotation).testTag("translator_swap_languages")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = "Inverser les langues",
                            tint = theme.suggestionTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Target Language Selector
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.actionKeyBackground)
                                .clickable { openLanguageMenu = if (openLanguageMenu == "target") null else "target" }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(targetLanguage.flag, fontSize = 12.sp)
                            Text(
                                text = targetLanguage.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.actionKeyTextColor
                            )
                        }

                    }
                }

                // Quick Insert translated text button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (translatedText.isNotEmpty()) Color(0xFF2E7D32) else theme.keyBackground)
                        .clickable(enabled = translatedText.isNotEmpty()) {
                            onCommitText(translatedText)
                            playFeedback()
                            Toast.makeText(context, "Traduction insérée !", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("translator_insert_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Insérer",
                            tint = if (translatedText.isNotEmpty()) Color.White else theme.keySubTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Insérer",
                            color = if (translatedText.isNotEmpty()) Color.White else theme.keySubTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Input Source Box (Type, paste or speak)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.keyBackground.copy(alpha = 0.6f))
                    .border(1.dp, theme.toolbarBackground, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (sourceText.isEmpty()) {
                            Text(
                                text = "Écrivez ici en ${sourceLanguage.name}...",
                                color = theme.keySubTextColor,
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = sourceText,
                            onValueChange = onSourceTextChange,
                            readOnly = true,
                            textStyle = TextStyle(
                                color = theme.keyTextColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(theme.actionKeyBackground),
                            modifier = Modifier.fillMaxSize().testTag("translator_input_field")
                        )
                    }

                    // Source Actions: Clear / Paste
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (sourceText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onSourceTextChange("")
                                    translatedText = ""
                                    playFeedback()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Effacer",
                                    tint = theme.keySubTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        onSourceTextChange(clip)
                                        playFeedback()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentPaste,
                                    contentDescription = "Coller",
                                    tint = theme.keySubTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Translated Output Box (Live translation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.actionKeyBackground.copy(alpha = 0.15f))
                    .border(1.dp, theme.actionKeyBackground.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when {
                            isLoading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = theme.suggestionTextColor
                                    )
                                    Text(
                                        text = "Traduction en cours...",
                                        color = theme.suggestionTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            translatedText.isNotEmpty() -> {
                                Text(
                                    text = translatedText,
                                    color = theme.keyTextColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            errorMessage != null -> {
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFE53935),
                                    fontSize = 12.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = "Traduction en ${targetLanguage.name}...",
                                    color = theme.keySubTextColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Target actions: Pronounce / Copy
                    if (translatedText.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    playFeedback()
                                    try {
                                        textToSpeech?.language = targetLanguage.locale
                                        textToSpeech?.speak(
                                            translatedText,
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            "trans_tts"
                                        )
                                    } catch (e: Exception) { /* ignore */ }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeUp,
                                    contentDescription = "Écouter",
                                    tint = theme.suggestionTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    playFeedback()
                                    clipboardManager.setText(AnnotatedString(translatedText))
                                    Toast.makeText(context, "Copié dans le presse-papiers", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copier",
                                    tint = theme.suggestionTextColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Conversation Phrases Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val quickPhrases = listOf(
                    "Bonjour",
                    "Comment allez-vous ?",
                    "Merci beaucoup !",
                    "S'il vous plaît",
                    "Où se trouve... ?",
                    "Combien ça coûte ?",
                    "Enchanté de faire votre connaissance",
                    "Bonne journée !",
                    "À bientôt",
                    "Oui, d'accord"
                )

                quickPhrases.forEach { phrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.keyBackground)
                            .clickable {
                                onSourceTextChange(phrase)
                                playFeedback()
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phrase,
                            color = theme.keyTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            }

            if (openLanguageMenu != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 42.dp, start = 6.dp, end = 6.dp)
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.toolbarBackground)
                        .verticalScroll(rememberScrollState())
                        .testTag("translator_language_options")
                ) {
                    SUPPORTED_TRANSLATE_LANGUAGES.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (openLanguageMenu == "source") sourceLanguage = language else targetLanguage = language
                                    openLanguageMenu = null
                                    playFeedback()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${language.flag}  ${language.name}",
                                color = theme.keyTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Online Translation using public MyMemory Translation REST API
private suspend fun performTranslation(query: String, sourceLang: String, targetLang: String): String {
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val urlString = "https://api.mymemory.translated.net/get?q=$encodedQuery&langpair=$sourceLang|$targetLang"
    val url = URL(urlString)
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 4000
        readTimeout = 4000
    }

    try {
        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            val json = JSONObject(sb.toString())
            val responseData = json.optJSONObject("responseData")
            val translated = responseData?.optString("translatedText")
            if (!translated.isNullOrBlank()) {
                return android.text.Html.fromHtml(translated, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
            }
        }
    } finally {
        conn.disconnect()
    }
    throw RuntimeException("Online translation returned no match")
}

// High Quality Offline Translation Dictionary for Instant Fallback
private val OFFLINE_DICTIONARY: Map<String, Map<String, String>> = mapOf(
    "bonjour" to mapOf("en" to "Hello", "es" to "Hola", "de" to "Hallo", "it" to "Ciao", "pt" to "Olá", "ar" to "مرحبا", "zh" to "你好", "ja" to "こんにちは", "ru" to "Здравствуйте", "sw" to "Habari"),
    "salut" to mapOf("en" to "Hi", "es" to "Hola", "de" to "Hi", "it" to "Ciao", "pt" to "Oi", "ar" to "أهلا", "zh" to "嗨", "ja" to "やあ", "ru" to "Привет", "sw" to "Jambo"),
    "bonsoir" to mapOf("en" to "Good evening", "es" to "Buenas noches", "de" to "Guten Abend", "it" to "Buonasera", "pt" to "Boa noite", "ar" to "مساء الخير", "zh" to "晚上好", "ja" to "こんばんは", "ru" to "Добрый вечер", "sw" to "Habari za jioni"),
    "merci" to mapOf("en" to "Thank you", "es" to "Gracias", "de" to "Danke", "it" to "Grazie", "pt" to "Obrigado", "ar" to "شكرا", "zh" to "谢谢", "ja" to "ありがとう", "ru" to "Спасибо", "sw" to "Asante"),
    "merci beaucoup" to mapOf("en" to "Thank you very much", "es" to "Muchas gracias", "de" to "Vielen Dank", "it" to "Grazie mille", "pt" to "Muito obrigado", "ar" to "شكرا جزيلا", "zh" to "非常感谢", "ja" to "どうもありがとうございます", "ru" to "Большое спасибо", "sw" to "Asante sana"),
    "merci beaucoup !" to mapOf("en" to "Thank you very much!", "es" to "¡Muchas gracias!", "de" to "Vielen Dank!", "it" to "Grazie mille!", "pt" to "Muito obrigado!", "ar" to "شكرا جزيلا!", "zh" to "非常感谢！", "ja" to "どうもありがとうございます！", "ru" to "Большое спасибо!", "sw" to "Asante sana!"),
    "de rien" to mapOf("en" to "You're welcome", "es" to "De nada", "de" to "Bitte sehr", "it" to "Prego", "pt" to "De nada", "ar" to "عفوا", "zh" to "不客气", "ja" to "どういたしまして", "ru" to "Пожалуйста", "sw" to "Karibu"),
    "s'il vous plaît" to mapOf("en" to "Please", "es" to "Por favor", "de" to "Bitte", "it" to "Per favore", "pt" to "Por favor", "ar" to "من فضلك", "zh" to "请", "ja" to "お願いします", "ru" to "Пожалуйста", "sw" to "Tafadhali"),
    "oui" to mapOf("en" to "Yes", "es" to "Sí", "de" to "Ja", "it" to "Sì", "pt" to "Sim", "ar" to "نعم", "zh" to "是", "ja" to "はい", "ru" to "Да", "sw" to "Ndiyo"),
    "non" to mapOf("en" to "No", "es" to "No", "de" to "Nein", "it" to "No", "pt" to "Não", "ar" to "لا", "zh" to "不", "ja" to "いいえ", "ru" to "Нет", "sw" to "Hapana"),
    "comment allez-vous ?" to mapOf("en" to "How are you?", "es" to "¿Cómo estás?", "de" to "Wie geht es Ihnen?", "it" to "Come sta?", "pt" to "Como vai?", "ar" to "كيف حالك؟", "zh" to "你好吗？", "ja" to "お元気ですか？", "ru" to "Как дела?", "sw" to "Habari gani?"),
    "comment vas-tu ?" to mapOf("en" to "How are you?", "es" to "¿Cómo estás?", "de" to "Wie geht's?", "it" to "Come stai?", "pt" to "Como você está?", "ar" to "كيف حالك؟", "zh" to "你怎么样？", "ja" to "元気？", "ru" to "Как поживаешь?", "sw" to "Hujambo?"),
    "où se trouve... ?" to mapOf("en" to "Where is...?", "es" to "¿Dónde está...?", "de" to "Wo ist...?", "it" to "Dov'è...?", "pt" to "Onde fica...?", "ar" to "أين يقع...؟", "zh" to "...在哪里？", "ja" to "…はどこですか？", "ru" to "Где находится...?", "sw" to "Iko wapi...?"),
    "combien ça coûte ?" to mapOf("en" to "How much does it cost?", "es" to "¿Cuánto cuesta?", "de" to "Wie viel kostet das?", "it" to "Quanto costa?", "pt" to "Quanto custa?", "ar" to "كم يكلف هذا؟", "zh" to "这个多少钱？", "ja" to "いくらですか？", "ru" to "Сколько это стоит?", "sw" to "Ni bei gani?"),
    "bonne journée !" to mapOf("en" to "Have a nice day!", "es" to "¡Que tengas un buen día!", "de" to "Schönen Tag!", "it" to "Buona giornata!", "pt" to "Tenha um bom dia!", "ar" to "أتمنى لك يوما سعيدا!", "zh" to "祝你有美好的一天！", "ja" to "良い一日を！", "ru" to "Хорошего дня!", "sw" to "Siku njema!"),
    "à bientôt" to mapOf("en" to "See you soon", "es" to "Hasta pronto", "de" to "Bis bald", "it" to "A presto", "pt" to "Até breve", "ar" to "إلى اللقاء قريبا", "zh" to "一会儿见", "ja" to "またね", "ru" to "До скорого", "sw" to "Tuonane baadaye"),
    "enchanté de faire votre connaissance" to mapOf("en" to "Nice to meet you", "es" to "Encantado de conocerte", "de" to "Freut mich, Sie kennenzulernen", "it" to "Piacere di conoscerti", "pt" to "Prazer em conhecê-lo", "ar" to "تشرفت بمعرفتك", "zh" to "很高兴认识你", "ja" to "はじめまして", "ru" to "Приятно познакомиться", "sw" to "Nimefurahi kukutana nawe"),
    "aidez-moi" to mapOf("en" to "Help me", "es" to "Ayúdame", "de" to "Helfen Sie mir", "it" to "Aiutami", "pt" to "Ajude-me", "ar" to "ساعدني", "zh" to "请帮帮我", "ja" to "助けてください", "ru" to "Помогите мне", "sw" to "Nisaidie")
)

private fun getOfflineTranslation(query: String, sourceLang: String, targetLang: String): String? {
    val lower = query.lowercase().trim()
    val dictEntry = OFFLINE_DICTIONARY[lower]
    if (dictEntry != null && dictEntry.containsKey(targetLang)) {
        return dictEntry[targetLang]
    }
    // Also check reverse if query matches target value
    for ((frKey, transMap) in OFFLINE_DICTIONARY) {
        if (transMap[sourceLang]?.lowercase() == lower) {
            return if (targetLang == "fr") frKey else transMap[targetLang]
        }
    }
    return null
}
