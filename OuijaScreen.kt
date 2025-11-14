package com.ruhan.possessao.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhan.possessao.app.MainViewModel
import kotlinx.coroutines.launch
import android.os.VibratorManager

@Composable
fun OuijaScreen(vm: MainViewModel, onBack: () -> Unit) {
    val resultState by vm.result.collectAsState()
    val demonName = resultState?.entityId?.replaceFirstChar { it.uppercase() } ?: "a entidade"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf("Toque para iniciar a conversa...") }

    // ------- EFEITOS -------
    val redPulse = remember { Animatable(0f) }     // overlay vermelho (alpha)
    val textFlicker = remember { Animatable(1f) }  // fade do texto
    val shakeOffset = remember { Animatable(0f) }  // micro-tremor

    val vignette = Brush.radialGradient(
        colors = listOf(Color(0xFF000000), Color(0xFF000000), Color(0xFF000000)),
        center = androidx.compose.ui.geometry.Offset.Unspecified,
        radius = Float.POSITIVE_INFINITY
    )

    val demonReplies = listOf(
        "Você me chamou, $demonName...",
        "$demonName está ouvindo.",
        "Sim...",
        "Não...",
        "Talvez...",
        "O que você procura já te encontrou.",
        "Silêncio... $demonName está perto.",
        "Eu estava esperando por você.",
        "$demonName sussurra o seu nome.",
        "Você pertence a $demonName agora.",
        "A voz dentro de você... é $demonName.",
        "$demonName gosta de te observar dormindo.",
        "O reflexo no espelho é $demonName sorrindo.",
        "O frio que sente agora... é minha mão.",
        "$demonName abriu a passagem.",
        "Não olhe para trás.",
        "O espelho sabe quem é $demonName.",
        "O chão está tremendo... ele chegou.",
        "A vela vai apagar em três... dois...",
        "$demonName gosta do seu medo.",
        "O quarto escureceu por minha causa.",
        "Não há mais saída.",
        "$demonName espera por você no espelho.",
        "Sinta isso... é o calor do inferno chegando."
    )

    fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (it.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(80)
                }
            }
        }
    }

    fun vibrateStrong(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+: pegue o Vibrator via VibratorManager
                val vm = context.getSystemService(VibratorManager::class.java)
                val vib = vm?.defaultVibrator
                if (vib?.hasVibrator() == true) {
                    // pulso perceptível + mini eco
                    val timings = longArrayOf(0, 70, 40, 110)   // delay, on, off, on
                    val amps    = intArrayOf(0, 200, 0, 255)    // 0..255
                    vib.vibrate(VibrationEffect.createWaveform(timings, amps, -1))
                    return
                }
            } else {
                // Pré-Android 12
                @Suppress("DEPRECATION")
                val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vib?.hasVibrator() == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 40, 110), -1))
                    } else {
                        vib.vibrate(140) // fallback bem simples
                    }
                    return
                }
            }
        } catch (_: Throwable) {
            // silencia qualquer OEM bugado
        }
    }

    fun onDemonReply() {
        message = demonReplies.random()

        scope.launch {
            shakeOffset.snapTo(0f)
            val kf = keyframes {
                durationMillis = 120
                -4f at 20; 4f at 40; -3f at 60; 3f at 80; 0f at 120
            }
            shakeOffset.animateTo(0f, kf)
        }
        scope.launch {
            redPulse.snapTo(0f)
            redPulse.animateTo(0.18f, tween(160, easing = LinearEasing))
            redPulse.animateTo(0f, tween(220, easing = LinearEasing))
        }
        scope.launch {
            textFlicker.snapTo(0.35f)
            textFlicker.animateTo(1f, tween(220, easing = LinearEasing))
        }
        vibrate(context)
        vibrateStrong(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)                // fundo constante
            .offset(x = shakeOffset.value.dp)       // micro-tremor
            .clickable(                             // clique no container (não bloqueia o botão)
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDemonReply() },
        contentAlignment = Alignment.Center
    ) {
        // Vignette nas bordas
        Box(
            Modifier
                .matchParentSize()
                .background(vignette)
        )

        // Overlay vermelho com alpha animado (pulso)
        Box(
            Modifier
                .matchParentSize()
                .background(Color(0xFF3B0000))
                .alpha(redPulse.value)
        )

        // Texto
        Text(
            text = message,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.Center)
                .alpha(textFlicker.value),
            lineHeight = 28.sp
        )

        // Botão — como é um filho clicável, consome o click e impede o onDemonReply do container
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Encerrar Sessão", color = Color.White)
        }
    }
}
