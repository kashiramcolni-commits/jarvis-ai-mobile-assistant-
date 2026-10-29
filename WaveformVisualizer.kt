package com.example.jarvis.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow

@Composable
fun WaveformVisualizer(
    isListening: Boolean,
    audioLevel: Float, // 0 to 12
    modifier: Modifier = Modifier
) {
    val barCount = 18
    val infiniteTransition = rememberInfiniteTransition(label = "waveformTransition")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animDuration = 350 + (i * 45) % 250
            val pulseAnim by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barPulse_$i"
            )

            val heightFactor = if (isListening) {
                val distFromCenter = kotlin.math.abs(i - barCount / 2f) / (barCount / 2f)
                val base = (1f - distFromCenter * 0.4f)
                val dynamicLevel = (audioLevel / 12f).coerceIn(0.15f, 1.0f)
                base * dynamicLevel * pulseAnim
            } else {
                0.15f
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.dp)
                    .fillMaxHeight(fraction = heightFactor.coerceIn(0.1f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isListening) ArcCyanGlow else ArcCyan.copy(alpha = 0.35f)
                    )
            )
        }
    }
}
