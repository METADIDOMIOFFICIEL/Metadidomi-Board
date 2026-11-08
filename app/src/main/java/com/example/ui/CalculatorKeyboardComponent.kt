package com.example.ui

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Keyboard
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
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class CalcMode {
    SIMPLE,
    SCIENTIFIC
}

@Composable
fun CalculatorKeyboardView(
    theme: KeyboardThemeColors,
    preferences: MBoardPreferences,
    onCommitText: (String) -> Unit,
    onBackToAlpha: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var calcMode by remember { mutableStateOf(CalcMode.SIMPLE) }
    var expression by remember { mutableStateOf("0") }
    var evaluatedResult by remember { mutableStateOf("0") }
    var isRadMode by remember { mutableStateOf(false) }

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

    // Mathematical Evaluator for Calculator
    fun evaluateMathExpression(expr: String, inRadians: Boolean): String {
        return try {
            val clean = expr.replace("×", "*").replace("÷", "/")
            val result = SafeMathParser(clean, inRadians).parse()
            if (result.isNaN() || result.isInfinite()) {
                "Erreur"
            } else {
                val df = DecimalFormat("#.########")
                df.format(result).replace(",", ".")
            }
        } catch (e: Exception) {
            "..."
        }
    }

    fun appendToken(token: String) {
        if (expression == "0" && token != "." && !listOf("+", "-", "×", "÷").contains(token)) {
            expression = token
        } else {
            expression += token
        }
        evaluatedResult = evaluateMathExpression(expression, isRadMode)
        playFeedback()
    }

    fun clearAll() {
        expression = "0"
        evaluatedResult = "0"
        playFeedback()
    }

    fun deleteLast() {
        if (expression.length > 1) {
            expression = expression.dropLast(1)
        } else {
            expression = "0"
        }
        evaluatedResult = evaluateMathExpression(expression, isRadMode)
        playFeedback()
    }

    val calculatedHeight = remember(preferences.keyHeight, preferences.keyboardScale) {
        (275f * preferences.keyHeight.multiplier * preferences.keyboardScale).dp.coerceIn(250.dp, 395.dp)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(calculatedHeight),
        color = theme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Header: Display + Modes (Simple vs Scientifique) + Return
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.toolbarBackground)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Return button
                IconButton(
                    onClick = {
                        onBackToAlpha()
                        playFeedback()
                    },
                    modifier = Modifier.size(30.dp).testTag("calc_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "Retour clavier",
                        tint = theme.keyTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Mode switch: Simple vs Scientifique
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (calcMode == CalcMode.SIMPLE) theme.actionKeyBackground else theme.keyBackground)
                            .clickable {
                                calcMode = CalcMode.SIMPLE
                                playFeedback()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("calc_mode_simple"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Simple",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (calcMode == CalcMode.SIMPLE) theme.actionKeyTextColor else theme.keyTextColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (calcMode == CalcMode.SCIENTIFIC) theme.actionKeyBackground else theme.keyBackground)
                            .clickable {
                                calcMode = CalcMode.SCIENTIFIC
                                playFeedback()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("calc_mode_scientific"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Scientifique",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (calcMode == CalcMode.SCIENTIFIC) theme.actionKeyTextColor else theme.keyTextColor
                        )
                    }
                }

                // Quick Insert result into text field button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2E7D32))
                        .clickable {
                            val toInsert = if (evaluatedResult != "..." && evaluatedResult != "Erreur") evaluatedResult else expression
                            onCommitText(toInsert)
                            playFeedback()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("calc_insert_result"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Insérer $evaluatedResult",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Calculation Screen Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.keyBackground.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (calcMode == CalcMode.SCIENTIFIC) (if (isRadMode) "RAD" else "DEG") else "",
                        fontSize = 10.sp,
                        color = theme.keySubTextColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expression,
                            fontSize = 15.sp,
                            color = theme.keyTextColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (evaluatedResult != "..." && evaluatedResult != "Erreur" && evaluatedResult != expression) {
                            Text(
                                text = " = $evaluatedResult",
                                fontSize = 15.sp,
                                color = theme.suggestionTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Keyboards according to mode - Fully flexible and adapted to keyboard bounds
            when (calcMode) {
                CalcMode.SIMPLE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Row 1: C, ( ), %, ÷
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("C", Color(0xFFFF5252), Color.White, Modifier.weight(1f)) { clearAll() }
                            CalcKey("(", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken("(") }
                            CalcKey(")", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken(")") }
                            CalcKey("%", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken("%") }
                            CalcKey("÷", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("÷") }
                        }

                        // Row 2: 7, 8, 9, ×, ⌫
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("7", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("7") }
                            CalcKey("8", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("8") }
                            CalcKey("9", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("9") }
                            CalcKey("×", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("×") }
                            CalcKey("⌫", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { deleteLast() }
                        }

                        // Row 3: 4, 5, 6, -
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("4", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("4") }
                            CalcKey("5", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("5") }
                            CalcKey("6", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("6") }
                            CalcKey("-", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("-") }
                            CalcKey("±", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) {
                                expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
                                evaluatedResult = evaluateMathExpression(expression, isRadMode)
                                playFeedback()
                            }
                        }

                        // Row 4: 1, 2, 3, +
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("1", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("1") }
                            CalcKey("2", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("2") }
                            CalcKey("3", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("3") }
                            CalcKey("+", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("+") }
                            CalcKey("=", Color(0xFF1E88E5), Color.White, Modifier.weight(1f)) {
                                val res = evaluateMathExpression(expression, isRadMode)
                                if (res != "Erreur" && res != "...") {
                                    expression = res
                                }
                                playFeedback()
                            }
                        }

                        // Row 5: 0, ., Insérer formule
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("0", theme.keyBackground, theme.keyTextColor, Modifier.weight(1.5f)) { appendToken("0") }
                            CalcKey(".", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken(".") }
                            Box(
                                modifier = Modifier
                                    .weight(2.5f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF00897B))
                                    .clickable {
                                        val full = "$expression = $evaluatedResult"
                                        onCommitText(full)
                                        playFeedback()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Insérer formule",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                CalcMode.SCIENTIFIC -> {
                    // Complete Scientific Keyboard Layout (All 6 rows adapt cleanly to keyboard height)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Scientific Row 1: RAD/DEG, sin, cos, tan, ln, log
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey(if (isRadMode) "RAD" else "DEG", Color(0xFF5C6BC0), Color.White, Modifier.weight(1f), fontSize = 10.sp) {
                                isRadMode = !isRadMode
                                evaluatedResult = evaluateMathExpression(expression, isRadMode)
                                playFeedback()
                            }
                            CalcKey("sin", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f), fontSize = 11.sp) { appendToken("sin(") }
                            CalcKey("cos", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f), fontSize = 11.sp) { appendToken("cos(") }
                            CalcKey("tan", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f), fontSize = 11.sp) { appendToken("tan(") }
                            CalcKey("ln", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f), fontSize = 11.sp) { appendToken("ln(") }
                            CalcKey("log", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f), fontSize = 11.sp) { appendToken("log(") }
                        }

                        // Scientific Row 2: π, e, √, ^, (, )
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("π", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken("π") }
                            CalcKey("e", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken("e") }
                            CalcKey("√", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken("√(") }
                            CalcKey("^", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken("^") }
                            CalcKey("(", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken("(") }
                            CalcKey(")", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { appendToken(")") }
                        }

                        // Row 3: 7, 8, 9, ÷, C, ⌫
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("7", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("7") }
                            CalcKey("8", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("8") }
                            CalcKey("9", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("9") }
                            CalcKey("÷", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("÷") }
                            CalcKey("C", Color(0xFFFF5252), Color.White, Modifier.weight(1f)) { clearAll() }
                            CalcKey("⌫", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) { deleteLast() }
                        }

                        // Row 4: 4, 5, 6, ×, x², 1/x
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("4", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("4") }
                            CalcKey("5", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("5") }
                            CalcKey("6", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("6") }
                            CalcKey("×", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("×") }
                            CalcKey("x²", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f), fontSize = 10.5.sp) { appendToken("^2") }
                            CalcKey("1/x", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f), fontSize = 10.sp) { appendToken("^(-1)") }
                        }

                        // Row 5: 1, 2, 3, -, 0, .
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("1", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("1") }
                            CalcKey("2", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("2") }
                            CalcKey("3", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("3") }
                            CalcKey("-", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("-") }
                            CalcKey("0", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken("0") }
                            CalcKey(".", theme.keyBackground, theme.keyTextColor, Modifier.weight(1f)) { appendToken(".") }
                        }

                        // Row 6: ±, +, =, Insérer formule
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CalcKey("±", theme.specialKeyBackground, theme.specialKeyTextColor, Modifier.weight(1f)) {
                                expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
                                evaluatedResult = evaluateMathExpression(expression, isRadMode)
                                playFeedback()
                            }
                            CalcKey("+", theme.actionKeyBackground, theme.actionKeyTextColor, Modifier.weight(1f)) { appendToken("+") }
                            CalcKey("=", Color(0xFF1E88E5), Color.White, Modifier.weight(2f)) {
                                val res = evaluateMathExpression(expression, isRadMode)
                                if (res != "Erreur" && res != "...") {
                                    expression = res
                                }
                                playFeedback()
                            }
                            Box(
                                modifier = Modifier
                                    .weight(2f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF00897B))
                                    .clickable {
                                        val full = "$expression = $evaluatedResult"
                                        onCommitText(full)
                                        playFeedback()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Insérer",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcKey(
    label: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        val dynamicFontSize = fontSize ?: when {
            label.length >= 6 -> 10.sp
            label.length >= 3 -> 11.5.sp
            label.length == 2 -> 13.sp
            else -> 16.sp
        }
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = dynamicFontSize,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// Safe Parser supporting +, -, *, /, %, ^, sin, cos, tan, ln, log, sqrt, pi, e, parentheses
class SafeMathParser(private val str: String, private val inRadians: Boolean) {
    private var pos = -1
    private var ch = ' '

    private fun nextChar() {
        pos++
        ch = if (pos < str.length) str[pos] else '\u0000'
    }

    private fun eat(charToEat: Char): Boolean {
        while (ch == ' ') nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < str.length && ch != '\u0000') throw RuntimeException("Caractère inattendu : $ch")
        return x
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            when {
                eat('+') -> x += parseTerm()
                eat('-') -> x -= parseTerm()
                else -> return x
            }
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            when {
                eat('*') -> x *= parseFactor()
                eat('/') -> x /= parseFactor()
                eat('%') -> x %= parseFactor()
                else -> return x
            }
        }
    }

    private fun parseFactor(): Double {
        if (eat('+')) return parseFactor()
        if (eat('-')) return -parseFactor()

        var x: Double
        val startPos = pos
        if (eat('(')) {
            x = parseExpression()
            eat(')')
        } else if ((ch in '0'..'9') || ch == '.') {
            while ((ch in '0'..'9') || ch == '.') nextChar()
            x = str.substring(startPos, pos).toDouble()
        } else if (eat('π')) {
            x = PI
        } else if (eat('e')) {
            x = Math.E
        } else if (ch in 'a'..'z' || ch in 'A'..'Z' || ch == '√') {
            while (ch in 'a'..'z' || ch in 'A'..'Z' || ch == '√') nextChar()
            val func = str.substring(startPos, pos)
            x = parseFactor()
            x = when (func) {
                "sqrt", "√" -> sqrt(x)
                "sin" -> if (inRadians) sin(x) else sin(Math.toRadians(x))
                "cos" -> if (inRadians) cos(x) else cos(Math.toRadians(x))
                "tan" -> if (inRadians) tan(x) else tan(Math.toRadians(x))
                "ln" -> ln(x)
                "log" -> log10(x)
                else -> throw RuntimeException("Fonction inconnue : $func")
            }
        } else {
            throw RuntimeException("Erreur de syntaxe")
        }

        if (eat('^')) x = x.pow(parseFactor())
        return x
    }
}
