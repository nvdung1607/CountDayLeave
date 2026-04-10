package com.example.countdayleave.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.countdayleave.ui.theme.FireworkColors
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// ---- Data model for one firework burst ----
private data class Particle(
    val startX: Float,
    val startY: Float,
    val angle: Float,          // radians
    val speed: Float,
    val color: Color,
    val size: Float
)

private data class FireworkBurst(
    val particles: List<Particle>,
    val startTimeMs: Long
)

@Composable
fun FireworksCanvas(modifier: Modifier = Modifier) {
    // Animation clock
    val infiniteTransition = rememberInfiniteTransition(label = "fireworks")
    val elapsed by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "elapsed"
    )

    // Generate bursts on first composition
    val bursts = remember { generateBursts(count = 8) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val currentMs = (elapsed * 6000L).toLong()

        bursts.forEach { burst ->
            val progress = ((currentMs - burst.startTimeMs + 6000L) % 6000L) / 2500f
            if (progress in 0f..1f) {
                drawBurst(burst, progress)
            }
        }
    }
}

private fun generateBursts(count: Int): List<FireworkBurst> {
    val spacing = 6000L / count
    return List(count) { i ->
        val numParticles = Random.nextInt(30, 50)
        val color = FireworkColors.random()
        FireworkBurst(
            particles = List(numParticles) {
                Particle(
                    startX  = Random.nextFloat() * 0.7f + 0.15f,  // normalized
                    startY  = Random.nextFloat() * 0.5f + 0.1f,
                    angle   = Random.nextFloat() * 2f * PI.toFloat(),
                    speed   = Random.nextFloat() * 0.25f + 0.12f,
                    color   = if (Random.nextFloat() > 0.3f) color else FireworkColors.random(),
                    size    = Random.nextFloat() * 5f + 3f
                )
            },
            startTimeMs = i * spacing
        )
    }
}

private fun DrawScope.drawBurst(burst: FireworkBurst, progress: Float) {
    // progress: 0 → 1 (burst lifetime)
    // Easing: fast out
    val easedProgress = 1f - (1f - progress).pow(2)
    val alpha = if (progress < 0.6f) 1f else (1f - (progress - 0.6f) / 0.4f).coerceIn(0f, 1f)

    burst.particles.forEach { p ->
        val x = (p.startX + cos(p.angle) * p.speed * easedProgress) * size.width
        val y = (p.startY + sin(p.angle) * p.speed * easedProgress) * size.height
        // gravity effect
        val gravityY = y + 0.03f * easedProgress * easedProgress * size.height

        drawCircle(
            color = p.color.copy(alpha = alpha),
            radius = p.size * (1f - progress * 0.5f),
            center = Offset(x, gravityY)
        )
    }
}
