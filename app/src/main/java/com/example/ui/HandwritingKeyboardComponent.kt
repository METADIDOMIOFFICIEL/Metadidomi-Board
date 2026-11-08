package com.example.ui

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MBoardPreferences
import com.example.ime.MBoardHaptics
import com.example.model.KeyboardThemeColors
import kotlin.math.abs
import kotlin.math.hypot

data class StrokeLine(val points: List<Offset>)

@Composable
fun HandwritingKeyboardView(
    theme: KeyboardThemeColors,
    preferences: MBoardPreferences,
    onCommitText: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    onBackToAlpha: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strokes = remember { mutableStateListOf<StrokeLine>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }
    var recognizedCandidates by remember { mutableStateOf<List<String>>(listOf("A", "O", "1", "E", "C")) }

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

    // Heuristic stroke recognizer for handwritten characters
    fun recognizeStrokes(strokeList: List<StrokeLine>): List<String> {
        if (strokeList.isEmpty()) return emptyList()

        val allPoints = strokeList.flatMap { it.points }
        if (allPoints.size < 4) return listOf(".", "-", ",")

        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }

        val width = (maxX - minX).coerceAtLeast(1f)
        val height = (maxY - minY).coerceAtLeast(1f)
        val aspectRatio = width / height
        val strokeCount = strokeList.size

        val candidates = mutableListOf<String>()

        // Analyze first and last point distance for closed shapes (O, 0, D, B)
        val firstStroke = strokeList.first().points
        val startPt = firstStroke.first()
        val endPt = firstStroke.last()
        val loopDistance = hypot(startPt.x - endPt.x, startPt.y - endPt.y)
        val isClosedLoop = loopDistance < (height * 0.35f) && firstStroke.size > 12

        when {
            // Horizontal stroke -> hyphen, minus, underline
            aspectRatio > 2.2f && strokeCount == 1 -> {
                candidates.addAll(listOf("-", "_", "一", "~"))
            }
            // Tall thin single stroke -> I, l, 1, |
            aspectRatio < 0.45f && strokeCount == 1 -> {
                candidates.addAll(listOf("I", "l", "1", "|", "!"))
            }
            // Closed loop -> O, 0, Q, D
            isClosedLoop && strokeCount == 1 -> {
                candidates.addAll(listOf("O", "0", "o", "D", "Q", "C"))
            }
            // 2 Strokes
            strokeCount == 2 -> {
                val s1 = strokeList[0].points
                val s2 = strokeList[1].points
                // Cross check for X or +
                val s1Mid = s1[s1.size / 2]
                val s2Mid = s2[s2.size / 2]
                val midDist = hypot(s1Mid.x - s2Mid.x, s1Mid.y - s2Mid.y)
                if (midDist < height * 0.4f) {
                    candidates.addAll(listOf("X", "+", "x", "t", "T"))
                } else {
                    candidates.addAll(listOf("T", "L", "V", "7", "P", "1"))
                }
            }
            // 3 Strokes -> A, H, N, Z, E, K
            strokeCount == 3 -> {
                candidates.addAll(listOf("A", "H", "N", "Z", "E", "K", "Y"))
            }
            // 4+ Strokes -> M, W, E, B
            strokeCount >= 4 -> {
                candidates.addAll(listOf("M", "W", "E", "B", "#", "&"))
            }
            // General 1 stroke shapes
            else -> {
                val startY = startPt.y
                val endY = endPt.y
                if (startY < minY + height * 0.3f && endY < minY + height * 0.3f) {
                    candidates.addAll(listOf("U", "V", "C", "S"))
                } else {
                    candidates.addAll(listOf("C", "S", "J", "L", "2", "3", "5", "6", "8", "9", "a", "e"))
                }
            }
        }

        // Fill default fallbacks
        val defaults = listOf("A", "B", "C", "D", "E", "Bonjour", "Merci")
        for (d in defaults) {
            if (!candidates.contains(d) && candidates.size < 7) {
                candidates.add(d)
            }
        }
        return candidates.take(7)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        color = theme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Candidate recognition strip + Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(theme.toolbarBackground)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Return to normal keyboard
                IconButton(
                    onClick = {
                        onBackToAlpha()
                        playFeedback()
                    },
                    modifier = Modifier.size(32.dp).testTag("handwriting_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "Clavier normal",
                        tint = theme.keyTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recognized candidate chips
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recognizedCandidates.forEachIndexed { idx, candidate ->
                        val isFirst = idx == 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isFirst) theme.actionKeyBackground else theme.keyBackground)
                                .clickable {
                                    onCommitText(candidate)
                                    strokes.clear()
                                    currentPoints.clear()
                                    recognizedCandidates = listOf("A", "E", "O", "1")
                                    playFeedback()
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("handwriting_candidate_$candidate"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = candidate,
                                color = if (isFirst) theme.actionKeyTextColor else theme.keyTextColor,
                                fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Clear canvas button
                IconButton(
                    onClick = {
                        strokes.clear()
                        currentPoints.clear()
                        recognizedCandidates = listOf("A", "O", "1", "E")
                        playFeedback()
                    },
                    modifier = Modifier.size(32.dp).testTag("handwriting_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Effacer",
                        tint = theme.keySubTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Central Handwriting Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.keyBackground.copy(alpha = 0.35f))
                    .border(1.2.dp, theme.suggestionTextColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    strokes.add(StrokeLine(currentPoints.toList()))
                                    currentPoints.clear()
                                    recognizedCandidates = recognizeStrokes(strokes)
                                    playFeedback()
                                }
                            },
                            onDragCancel = {
                                currentPoints.clear()
                            }
                        )
                    }
                    .testTag("handwriting_canvas"),
                contentAlignment = Alignment.Center
            ) {
                // Background watermarked drawing hint if canvas is empty
                if (strokes.isEmpty() && currentPoints.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gesture,
                            contentDescription = null,
                            tint = theme.keySubTextColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Écrivez ici avec votre doigt ou stylet...",
                            color = theme.keySubTextColor.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Drawing Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val inkColor = theme.suggestionTextColor

                    // Draw completed strokes
                    strokes.forEach { stroke ->
                        if (stroke.points.size > 1) {
                            val path = Path().apply {
                                moveTo(stroke.points[0].x, stroke.points[0].y)
                                for (i in 1 until stroke.points.size) {
                                    val prev = stroke.points[i - 1]
                                    val curr = stroke.points[i]
                                    quadraticTo(
                                        prev.x,
                                        prev.y,
                                        (prev.x + curr.x) / 2f,
                                        (prev.y + curr.y) / 2f
                                    )
                                }
                                lineTo(stroke.points.last().x, stroke.points.last().y)
                            }
                            drawPath(
                                path = path,
                                color = inkColor,
                                style = Stroke(
                                    width = 7.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Draw active in-progress stroke
                    if (currentPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPoints[0].x, currentPoints[0].y)
                            for (i in 1 until currentPoints.size) {
                                lineTo(currentPoints[i].x, currentPoints[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = inkColor,
                            style = Stroke(
                                width = 7.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Bottom Actions Row (Space, Delete, Enter, Commit)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Backspace
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.specialKeyBackground)
                        .clickable {
                            onDelete()
                            playFeedback()
                        }
                        .testTag("handwriting_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Effacer",
                        tint = theme.specialKeyTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Spacebar
                Box(
                    modifier = Modifier
                        .weight(2.6f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.keyBackground)
                        .clickable {
                            onCommitText(" ")
                            strokes.clear()
                            currentPoints.clear()
                            playFeedback()
                        }
                        .testTag("handwriting_spacebar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Espace",
                        color = theme.keyTextColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                // Insert top candidate
                Box(
                    modifier = Modifier
                        .weight(1.8f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.actionKeyBackground)
                        .clickable {
                            val topCandidate = recognizedCandidates.firstOrNull() ?: " "
                            onCommitText(topCandidate)
                            strokes.clear()
                            currentPoints.clear()
                            recognizedCandidates = listOf("A", "O", "1")
                            playFeedback()
                        }
                        .testTag("handwriting_insert_top"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = theme.actionKeyTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Insérer",
                            color = theme.actionKeyTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Enter button
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.actionKeyBackground)
                        .clickable {
                            onEnter()
                            playFeedback()
                        }
                        .testTag("handwriting_enter"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = "Entrée",
                        tint = theme.actionKeyTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
