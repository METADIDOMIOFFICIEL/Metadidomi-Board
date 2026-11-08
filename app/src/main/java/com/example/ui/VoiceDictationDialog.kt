package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.data.MBoardPreferences
import com.example.model.KeyboardThemeColors
import java.util.Locale

@Composable
fun VoiceDictationSheet(
    theme: KeyboardThemeColors,
    onTextRecognized: (String) -> Unit,
    onDismiss: () -> Unit,
    preferences: MBoardPreferences? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var currentSpeechPartial by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Initialisation de la dictée vocale...") }
    var rmsAudioLevel by remember { mutableFloatStateOf(0f) }
    var speechRecognizerInstance by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var recognitionAvailable by remember { mutableStateOf(SpeechRecognizer.isRecognitionAvailable(context)) }

    // Pulsing transition for active listening (Gboard style)
    val infiniteTransition = rememberInfiniteTransition(label = "gboard_mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gboard_pulse_scale"
    )

    fun createSpeechIntent(): Intent {
        val localeTag = Locale.getDefault().toLanguageTag().ifEmpty { "fr-FR" }
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L)
        }
    }

    fun startListeningSession(recognizer: SpeechRecognizer?) {
        if (!hasAudioPermission) {
            statusMessage = "Autorisation microphone requise"
            isListening = false
            return
        }
        if (recognizer == null) {
            statusMessage = "Prêt. Appuyez sur le micro"
            isListening = false
            return
        }
        try {
            recognizer.cancel()
            val intent = createSpeechIntent()
            recognizer.startListening(intent)
            isListening = true
            currentSpeechPartial = ""
            statusMessage = "Écoute en cours... Parlez naturellement"
        } catch (e: Exception) {
            statusMessage = "Appuyez sur le micro pour parler"
            isListening = false
        }
    }

    // Safe permission request without LocalActivityResultRegistryOwner (safe for IME Service)
    fun requestAudioPermission() {
        val activity = context as? Activity
        if (activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
        } else {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(context, Class.forName("com.example.MainActivity")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e2: Exception) { /* ignore */ }
            }
        }
    }

    fun startListeningOrIntent() {
        hasAudioPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasAudioPermission) {
            requestAudioPermission()
            statusMessage = "Autorisation micro requise"
            return
        }
        if (recognitionAvailable && speechRecognizerInstance != null) {
            startListeningSession(speechRecognizerInstance)
        } else {
            try {
                val intent = createSpeechIntent().apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                statusMessage = "Dictée vocale démarrée..."
            } catch (e: Exception) {
                statusMessage = "Service vocal indisponible. Utilisez la ponctuation :"
            }
        }
    }

    // Setup SpeechRecognizer lifecycle safely in both Activity and Service
    DisposableEffect(hasAudioPermission) {
        var recognizer: SpeechRecognizer? = null
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        recognitionAvailable = available

        if (available && hasAudioPermission) {
            try {
                recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            statusMessage = "Prêt ! Parlez maintenant..."
                            isListening = true
                        }

                        override fun onBeginningOfSpeech() {
                            statusMessage = "Écoute de votre voix..."
                            isListening = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            rmsAudioLevel = (rmsdB.coerceIn(0f, 10f) / 10f)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            statusMessage = "Traitement STT..."
                            rmsAudioLevel = 0f
                        }

                        override fun onError(error: Int) {
                            rmsAudioLevel = 0f
                            isListening = false
                            statusMessage = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "Aucun mot détecté. Appuyez sur le micro."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Temps écoulé. Appuyez sur le micro."
                                SpeechRecognizer.ERROR_AUDIO -> "Problème audio. Réessayez."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission micro requise."
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Connexion requise pour la reconnaissance."
                                else -> "Micro en veille. Appuyez pour parler."
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            rmsAudioLevel = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val text = matches[0].trim()
                                if (text.isNotEmpty()) {
                                    recognizedText = text
                                    currentSpeechPartial = ""
                                    statusMessage = text
                                    onTextRecognized(text)
                                }
                            }
                            isListening = false
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val partial = matches[0].trim()
                                currentSpeechPartial = partial
                                statusMessage = partial
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
                speechRecognizerInstance = recognizer
                startListeningSession(recognizer)
            } catch (e: Exception) {
                statusMessage = "Appuyez sur le micro pour commencer"
                isListening = false
            }
        } else if (!hasAudioPermission) {
            statusMessage = "Autorisation microphone requise"
            isListening = false
        } else {
            statusMessage = "Service vocal prêt. Appuyez sur le micro"
            isListening = false
        }

        onDispose {
            try {
                recognizer?.stopListening()
                recognizer?.cancel()
                recognizer?.destroy()
            } catch (e: Exception) { /* ignore */ }
        }
    }

    // Adaptive height based on user keyboard preferences
    val containerHeight = if (preferences != null) {
        (270f * preferences.keyHeight.multiplier * preferences.keyboardScale).dp.coerceIn(245.dp, 385.dp)
    } else {
        275.dp
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight),
        color = theme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Gboard Top Bar: Language + Return to keyboard button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFF34A853) else Color(0xFFEA4335))
                    )
                    Text(
                        text = if (isListening) "Dictée vocale STT active" else "Dictée vocale (Pause)",
                        color = theme.suggestionTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }

                // Return to normal keyboard icon button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("voice_back_to_keyboard")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "Retour au clavier",
                        tint = theme.keyTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Real-time spoken text preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.keyBackground.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentSpeechPartial.isNotEmpty()) {
                        "\"$currentSpeechPartial\""
                    } else if (recognizedText.isNotEmpty()) {
                        "\"$recognizedText\""
                    } else {
                        statusMessage
                    },
                    color = if (currentSpeechPartial.isNotEmpty()) theme.actionKeyTextColor else theme.keyTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            // Permission Request Prompt if not granted
            if (!hasAudioPermission) {
                Button(
                    onClick = { requestAudioPermission() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("request_audio_permission_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Autoriser le microphone pour le STT", fontSize = 12.sp)
                }
            } else {
                // Central MBoard microphone with a theme-colored audio waveform
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // The waveform uses the active keyboard palette.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(28.dp)
                    ) {
                        val colors = listOf(
                            theme.suggestionTextColor,
                            theme.actionKeyBackground,
                            theme.specialKeyBackground,
                            theme.keyTextColor
                        )
                        colors.forEachIndexed { idx, barColor ->
                            val dynamicHeight = if (isListening) {
                                val waveBoost = when (idx) {
                                    0 -> 0.7f + (rmsAudioLevel * 0.9f)
                                    1 -> 1.0f + (rmsAudioLevel * 1.3f)
                                    2 -> 0.85f + (rmsAudioLevel * 1.1f)
                                    else -> 0.65f + (rmsAudioLevel * 0.8f)
                                }
                                (12.dp + (14.dp * waveBoost)).coerceIn(8.dp, 28.dp)
                            } else {
                                8.dp
                            }
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(dynamicHeight)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(barColor)
                            )
                        }
                    }

                    // Large dictation button
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .scale(if (isListening) (pulseScale + (rmsAudioLevel * 0.15f)) else 1.0f)
                            .clip(CircleShape)
                            .background(if (isListening) theme.actionKeyBackground else theme.keyBackground)
                            .clickable {
                                if (isListening) {
                                    try {
                                        speechRecognizerInstance?.stopListening()
                                        speechRecognizerInstance?.cancel()
                                    } catch (e: Exception) { /* ignore */ }
                                    isListening = false
                                    statusMessage = "Micro suspendu. Appuyez pour réactiver"
                                } else {
                                    startListeningOrIntent()
                                }
                            }
                            .testTag("gboard_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = "Microphone STT",
                            tint = if (isListening) Color.White else theme.keyTextColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = if (isListening) "Parlez... (Reconnaissance vocale STT)" else "Appuyez pour parler",
                        fontSize = 11.sp,
                        color = theme.keySubTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Punctuation & Expressions Bar (like Gboard Voice)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val quickPunctuation = listOf(
                    "." to "Point .",
                    "," to "Virgule ,",
                    "?" to "?",
                    "!" to "!",
                    "\n" to "↵ Ligne",
                    "Merci !" to "Merci"
                )

                quickPunctuation.forEach { (symbol, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.keyBackground)
                            .clickable {
                                onTextRecognized(symbol)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.keyTextColor
                        )
                    }
                }
            }
        }
    }
}
