package com.example.jarvis.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcBlue
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.ReactorCoreWhite
import com.example.ui.theme.ReactorGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class ReactorState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

@Composable
fun ArcReactor(
    state: ReactorState = ReactorState.IDLE,
    audioLevel: Float = 0f, // 0f to 12f
    reactorSize: Dp = 180.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

    // Outer ring rotation (slow clockwise)
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == ReactorState.PROCESSING) 3000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    // Inner ring rotation (counter clockwise)
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == ReactorState.PROCESSING) 2000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    // Breathing pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    ReactorState.LISTENING -> 400
                    ReactorState.SPEAKING -> 600
                    ReactorState.PROCESSING -> 300
                    else -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Audio reactive boost factor
    val audioBoost = if (state == ReactorState.LISTENING) {
        (audioLevel / 12f) * 0.35f
    } else 0f

    val effectivePulse = pulse + audioBoost

    val clickModifier = if (onClick != null) {
        Modifier.clip(CircleShape).clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .size(reactorSize)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.width / 2f) * 0.85f

            // 1. Ambient outer glow field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ArcCyan.copy(alpha = 0.25f * effectivePulse),
                        ElectricAzure.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.3f
                ),
                radius = baseRadius * 1.3f,
                center = center
            )

            // 2. Outer segmented technical ring (stator perimeter)
            rotate(outerRotation, pivot = center) {
                drawOuterStatorRing(center, baseRadius)
            }

            // 3. Counter-rotating inner energy coils ring
            rotate(innerRotation, pivot = center) {
                drawInnerMagneticCoils(center, baseRadius * 0.72f, state)
            }

            // 4. Concentric target reticles & guide arcs
            drawConcentricReticles(center, baseRadius, state, effectivePulse)

            // 5. Plasma Core (Iron Man arc reactor heart)
            drawPlasmaCore(center, baseRadius * 0.38f, state, effectivePulse)
        }
    }
}

private fun DrawScope.drawOuterStatorRing(center: Offset, radius: Float) {
    // Thin outer track
    drawCircle(
        color = ArcCyan.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 12 Outer tick marks / stator teeth
    val segments = 12
    for (i in 0 until segments) {
        val angleDeg = (i * 360f / segments)
        val angleRad = (angleDeg * PI / 180f).toFloat()

        val r1 = radius - 6.dp.toPx()
        val r2 = radius + 4.dp.toPx()

        val p1 = Offset(center.x + r1 * cos(angleRad), center.y + r1 * sin(angleRad))
        val p2 = Offset(center.x + r2 * cos(angleRad), center.y + r2 * sin(angleRad))

        drawLine(
            color = if (i % 3 == 0) ArcCyanGlow else ArcCyan.copy(alpha = 0.6f),
            start = p1,
            end = p2,
            strokeWidth = if (i % 3 == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // 4 major quadrant accent arcs
    for (i in 0 until 4) {
        val startAngle = i * 90f + 12f
        drawArc(
            color = ArcCyanGlow,
            startAngle = startAngle,
            sweepAngle = 22f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawInnerMagneticCoils(center: Offset, radius: Float, state: ReactorState) {
    val coilCount = 10
    val coilColor = when (state) {
        ReactorState.LISTENING -> ArcCyanGlow
        ReactorState.SPEAKING -> ElectricAzure
        ReactorState.PROCESSING -> ReactorGold
        else -> ArcCyan
    }

    for (i in 0 until coilCount) {
        val angleDeg = (i * 360f / coilCount)
        val angleRad = (angleDeg * PI / 180f).toFloat()

        val rOuter = radius
        val rInner = radius * 0.68f

        val x1 = center.x + rInner * cos(angleRad)
        val y1 = center.y + rInner * sin(angleRad)
        val x2 = center.x + rOuter * cos(angleRad)
        val y2 = center.y + rOuter * sin(angleRad)

        drawLine(
            color = coilColor.copy(alpha = 0.85f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Mid ring connecting coils
    drawCircle(
        color = ArcBlue.copy(alpha = 0.5f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawConcentricReticles(
    center: Offset,
    maxRadius: Float,
    state: ReactorState,
    pulse: Float
) {
    // Fine reticle circle
    drawCircle(
        color = ArcCyan.copy(alpha = 0.2f),
        radius = maxRadius * 0.52f * pulse,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // Crosshairs
    val crosshairLen = 8.dp.toPx()
    drawLine(
        color = ArcCyan.copy(alpha = 0.4f),
        start = Offset(center.x - crosshairLen, center.y),
        end = Offset(center.x + crosshairLen, center.y),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = ArcCyan.copy(alpha = 0.4f),
        start = Offset(center.x, center.y - crosshairLen),
        end = Offset(center.x, center.y + crosshairLen),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawPlasmaCore(
    center: Offset,
    radius: Float,
    state: ReactorState,
    pulse: Float
) {
    val effectiveRadius = radius * pulse

    val coreGlowColor = when (state) {
        ReactorState.LISTENING -> ArcCyanGlow
        ReactorState.SPEAKING -> ArcBlue
        ReactorState.PROCESSING -> ReactorGold
        else -> ArcCyan
    }

    // Outer plasma corona
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                coreGlowColor.copy(alpha = 0.9f),
                coreGlowColor.copy(alpha = 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = effectiveRadius * 1.5f
        ),
        radius = effectiveRadius * 1.5f,
        center = center
    )

    // Inner bright white energy core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                ReactorCoreWhite,
                coreGlowColor,
                ObsidianVoid.copy(alpha = 0.2f)
            ),
            center = center,
            radius = effectiveRadius
        ),
        radius = effectiveRadius,
        center = center
    )

    // Core boundary ring
    drawCircle(
        color = ReactorCoreWhite.copy(alpha = 0.9f),
        radius = effectiveRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
}
