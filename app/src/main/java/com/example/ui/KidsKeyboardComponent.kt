package com.example.ui

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MBoardPreferences
import com.example.ime.MBoardHaptics
import com.example.model.KeyboardThemeColors

enum class KidsKeyboardSubMode {
    LETTERS,
    NUMBERS_SHAPES,
    ANIMALS_TOYS
}

@Composable
fun KidsKeyboardView(
    theme: KeyboardThemeColors,
    preferences: MBoardPreferences,
    onCommitText: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    onBackToAlpha: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var subMode by remember { mutableStateOf(KidsKeyboardSubMode.LETTERS) }
    var isUppercase by remember { mutableStateOf(true) }

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

    val kidColors = listOf(
        Color(0xFFFF5252), // Candy Red
        Color(0xFFFF7043), // Orange
        Color(0xFFFFCA28), // Sunshine Yellow
        Color(0xFF66BB6A), // Fresh Green
        Color(0xFF26A69A), // Mint Teal
        Color(0xFF42A5F5), // Sky Blue
        Color(0xFFAB47BC), // Lavender Purple
        Color(0xFFEC407A)  // Bubblegum Pink
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        color = Color(0xFFFFF9E6) // Warm creamy joyful background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Kids Top Bar: Submodes + Quick words + Return
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tabs
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KidsTabButton(
                        label = "ABC",
                        isSelected = subMode == KidsKeyboardSubMode.LETTERS,
                        color = Color(0xFF42A5F5),
                        onClick = {
                            subMode = KidsKeyboardSubMode.LETTERS
                            playFeedback()
                        }
                    )
                    KidsTabButton(
                        label = "123 ⭐",
                        isSelected = subMode == KidsKeyboardSubMode.NUMBERS_SHAPES,
                        color = Color(0xFFFF9800),
                        onClick = {
                            subMode = KidsKeyboardSubMode.NUMBERS_SHAPES
                            playFeedback()
                        }
                    )
                    KidsTabButton(
                        label = "🦁 🚀",
                        isSelected = subMode == KidsKeyboardSubMode.ANIMALS_TOYS,
                        color = Color(0xFF66BB6A),
                        onClick = {
                            subMode = KidsKeyboardSubMode.ANIMALS_TOYS
                            playFeedback()
                        }
                    )
                }

                // Return to normal keyboard button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF5C6BC0))
                        .clickable {
                            onBackToAlpha()
                            playFeedback()
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("kids_back_to_normal"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Keyboard,
                            contentDescription = "Clavier normal",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Normal",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Kid Words Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val quickWords = listOf("Maman ❤️", "Papa ⭐", "Merci !", "Bonjour", "Bravo 🎈", "Oui", "Non")
                quickWords.forEachIndexed { idx, word ->
                    val color = kidColors[idx % kidColors.size]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.2f))
                            .border(1.dp, color, RoundedCornerShape(8.dp))
                            .clickable {
                                onCommitText(word.substringBefore(" ") + " ")
                                playFeedback()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = word,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            maxLines = 1
                        )
                    }
                }
            }

            // Body depending on sub-mode
            when (subMode) {
                KidsKeyboardSubMode.LETTERS -> {
                    // 3 Big Chunky Letter Rows (AZERTY or French ABC)
                    val r1 = listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P")
                    val r2 = listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M")
                    val r3 = listOf("W", "X", "C", "V", "B", "N", "É", "È")

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Row 1
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            r1.forEachIndexed { i, letter ->
                                val text = if (isUppercase) letter else letter.lowercase()
                                KidsKey(
                                    label = text,
                                    color = kidColors[i % kidColors.size],
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onCommitText(text)
                                        playFeedback()
                                    }
                                )
                            }
                        }

                        // Row 2
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            r2.forEachIndexed { i, letter ->
                                val text = if (isUppercase) letter else letter.lowercase()
                                KidsKey(
                                    label = text,
                                    color = kidColors[(i + 3) % kidColors.size],
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onCommitText(text)
                                        playFeedback()
                                    }
                                )
                            }
                        }

                        // Row 3 (with Caps toggle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            // Caps toggle
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isUppercase) Color(0xFFFF9800) else Color(0xFFFFE0B2))
                                    .clickable {
                                        isUppercase = !isUppercase
                                        playFeedback()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isUppercase) "ABC" else "abc",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = if (isUppercase) Color.White else Color(0xFFE65100)
                                )
                            }

                            r3.forEachIndexed { i, letter ->
                                val text = if (isUppercase) letter else letter.lowercase()
                                KidsKey(
                                    label = text,
                                    color = kidColors[(i + 5) % kidColors.size],
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onCommitText(text)
                                        playFeedback()
                                    }
                                )
                            }

                            // Backspace
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFF5252))
                                    .clickable {
                                        onDelete()
                                        playFeedback()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Effacer",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                KidsKeyboardSubMode.NUMBERS_SHAPES -> {
                    val numbersRow1 = listOf("1", "2", "3", "4", "5")
                    val numbersRow2 = listOf("6", "7", "8", "9", "0")
                    val shapes = listOf("⭐", "🎈", "☀️", "🌸", "🔺", "🟢", "🟦", "🔶", "🌈", "🎁")

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            numbersRow1.forEachIndexed { i, num ->
                                KidsKey(
                                    label = num,
                                    color = kidColors[i % kidColors.size],
                                    fontSize = 24.sp,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onCommitText(num)
                                        playFeedback()
                                    }
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            numbersRow2.forEachIndexed { i, num ->
                                KidsKey(
                                    label = num,
                                    color = kidColors[(i + 2) % kidColors.size],
                                    fontSize = 24.sp,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onCommitText(num)
                                        playFeedback()
                                    }
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            shapes.forEach { shape ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp))
                                        .clickable {
                                            onCommitText(shape)
                                            playFeedback()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = shape, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                KidsKeyboardSubMode.ANIMALS_TOYS -> {
                    val animals = listOf("🐶", "🐱", "🦁", "🐘", "🐼", "🐰", "🐸", "🐵", "🦄", "🐧")
                    val toys = listOf("🚗", "🚀", "🚂", "✈️", "🚲", "🧸", "🎨", "⚽", "🎮", "🍦")

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            animals.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                                        .clickable {
                                            onCommitText(emoji)
                                            playFeedback()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 28.sp)
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            toys.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.5.dp, Color(0xFF64B5F6), RoundedCornerShape(12.dp))
                                        .clickable {
                                            onCommitText(emoji)
                                            playFeedback()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 28.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Big Spacebar Row for Kids
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button (always visible in numbers & animals)
                if (subMode != KidsKeyboardSubMode.LETTERS) {
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF5252))
                            .clickable {
                                onDelete()
                                playFeedback()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Effacer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Big Spacebar
                Box(
                    modifier = Modifier
                        .weight(3.5f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF42A5F5))
                        .clickable {
                            onCommitText(" ")
                            playFeedback()
                        }
                        .testTag("kids_spacebar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Espace 🚀",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                // Big Enter button
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF66BB6A))
                        .clickable {
                            onEnter()
                            playFeedback()
                        }
                        .testTag("kids_enter"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                            contentDescription = "Entrée",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Valider",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KidsTabButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color else color.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF2C3E50),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun KidsKey(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}
