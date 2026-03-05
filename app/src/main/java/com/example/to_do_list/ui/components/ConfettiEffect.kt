package com.example.to_do_list.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float,
    val y: Float,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float
)

private val confettiColors = listOf(
    Color(0xFF43A047),
    Color(0xFF66BB6A),
    Color(0xFFA5D6A7),
    Color(0xFF1B5E20),
    Color(0xFFB9F6CA),
    Color(0xFFFFD700),
    Color(0xFFFFFFFF)
)

@Composable
fun ConfettiEffect(
    active: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 120,
    durationMs: Int = 2000
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.4f, // naissent dans le haut de l'écran
                angle = Math.PI.toFloat() / 2f + Random.nextFloat() * Math.PI.toFloat() * 0.8f - Math.PI.toFloat() * 0.4f,
                speed = 0.3f + Random.nextFloat() * 0.7f,
                size = 8f + Random.nextFloat() * 16f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(active) {
        if (active) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(durationMillis = durationMs))
        }
    }

    if (active || progress.value > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val p = progress.value
            val gravity = 0.6f

            particles.forEach { particle ->
                val elapsed = p * particle.speed
                val px = particle.x * size.width + cos(particle.angle) * elapsed * size.width * 0.8f
                val py = particle.y * size.height + sin(particle.angle) * elapsed * size.height * 0.5f +
                        gravity * elapsed * elapsed * size.height * 0.5f
                val alpha = (1f - p * 1.2f).coerceIn(0f, 1f)
                val currentRotation = particle.rotation + particle.rotationSpeed * p

                if (alpha > 0f) {
                    rotate(currentRotation, pivot = Offset(px, py)) {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(px - particle.size / 2, py - particle.size / 4),
                            size = androidx.compose.ui.geometry.Size(particle.size, particle.size / 2)
                        )
                    }
                }
            }
        }
    }
}
