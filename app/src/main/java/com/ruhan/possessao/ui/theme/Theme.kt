package com.ruhan.possessao.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val HorrorColors = darkColorScheme(
    primary = Color(0xFF7B0A0A),       // vermelho escuro (sangue)
    secondary = Color(0xFF3A3A3A),     // cinza carvão
    background = Color(0xFF080808),    // quase preto
    surface = Color(0xFF0F0F0F),       // preto levemente diferenciado
    onPrimary = Color(0xFFF3EDE9),     // tom pálido para textos
    onBackground = Color(0xFFEDEDED),  // texto principal claro para melhor contraste
    onSurface = Color(0xFFEDEDED),
    error = Color(0xFFB00020),         // vermelho de alerta
    onError = Color(0xFFFFEDEF)
)

private val HorrorTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.2.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp
    )
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HorrorColors, typography = HorrorTypography) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
