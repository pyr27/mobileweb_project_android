package com.example.mw_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class FourBasicOpt {
    fun add(x: Double, y: Double) = x + y
    fun subtract(x: Double, y: Double) = x - y
    fun multiply(x: Double, y: Double) = x * y
    fun divide(x: Double, y: Double): Double {
        if (y == 0.0) return 0.0
        return x / y
    }
}

private val BackgroundBlack = Color(0xFF000000)
private val ButtonDark = Color(0xFF3A3A3A)
private val ButtonNumber = Color(0xFF505050)
private val ButtonAccent = Color(0xFF6C5CE7)
private val TextWhite = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBlack) {
                    CalculatorScreen()
                }
            }
        }
    }
}

private fun formatResult(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

@Composable
fun CalculatorScreen() {
    val opt = remember { FourBasicOpt() }

    // 화면에 그대로 보여줄 입력 전체 (예: "100+200-50")
    var expression by remember { mutableStateOf("") }
    // 방금 "="을 눌러서 결과가 표시된 상태인지 (다음 숫자는 새로 시작해야 함)
    var afterEquals by remember { mutableStateOf(false) }

    fun evaluateExpression(expr: String): Double {
        val tokens = Regex("(\\d+\\.?\\d*)|([+\\-×÷])").findAll(expr).map { it.value }.toList()
        if (tokens.isEmpty()) return 0.0
        var result = tokens[0].toDoubleOrNull() ?: 0.0
        var i = 1
        while (i + 1 <= tokens.size - 1) {
            val op = tokens[i]
            val nextNum = tokens[i + 1].toDoubleOrNull() ?: break
            result = when (op) {
                "+" -> opt.add(result, nextNum)
                "-" -> opt.subtract(result, nextNum)
                "×" -> opt.multiply(result, nextNum)
                "÷" -> opt.divide(result, nextNum)
                else -> result
            }
            i += 2
        }
        return result
    }

    fun onDigit(d: String) {
        expression = if (afterEquals) {
            afterEquals = false
            d
        } else {
            expression + d
        }
    }

    fun onDot() {
        afterEquals = false
        val lastNumber = Regex("(\\d*\\.?\\d*)$").find(expression)?.value ?: ""
        if (!lastNumber.contains(".")) {
            expression += if (lastNumber.isEmpty()) "0." else "."
        }
    }

    fun onOperator(op: String) {
        afterEquals = false
        expression = when {
            expression.isEmpty() -> return
            expression.last() in listOf('+', '-', '×', '÷') -> expression.dropLast(1) + op
            else -> expression + op
        }
    }

    fun onPercent() {
        val match = Regex("(\\d+\\.?\\d*)$").find(expression) ?: return
        val num = match.value.toDoubleOrNull() ?: return
        expression = expression.substring(0, match.range.first) + formatResult(num / 100)
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
    }

    fun onEquals() {
        if (expression.isEmpty()) return
        val result = evaluateExpression(expression)
        expression = formatResult(result)
        afterEquals = true
    }

    fun onClear() {
        expression = ""
        afterEquals = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 입력/결과 표시 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = expression.ifEmpty { "0" },
                color = TextWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End,
                maxLines = 2
            )
        }

        val rows = listOf(
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    val isOperator = label in listOf("+", "-", "×", "÷", "=")
                    val isFunction = label in listOf("C", "⌫", "%")
                    val bg = when {
                        isFunction -> ButtonDark
                        isOperator -> ButtonAccent
                        else -> ButtonNumber
                    }
                    val weight = if (label == "0") 2f else 1f

                    CalcButton(
                        text = label,
                        modifier = Modifier.weight(weight),
                        backgroundColor = bg,
                        onClick = {
                            when (label) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                "%" -> onPercent()
                                "=" -> onEquals()
                                "+", "-", "×", "÷" -> onOperator(label)
                                "." -> onDot()
                                else -> onDigit(label)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(if (text == "0") 2f else 1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = TextWhite, fontSize = 24.sp)
    }
}