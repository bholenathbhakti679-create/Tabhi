package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TabahiAmber
import com.example.ui.theme.TabahiGold
import com.example.ui.theme.TabahiOrange
import com.example.ui.theme.TabahiOrangeDark
import com.example.ui.theme.TabahiOrangeLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // 3D Animation Controllers
    val scale = remember { Animatable(0.3f) }
    val rotationY = remember { Animatable(-90f) }
    val rotationX = remember { Animatable(25f) }
    val launchOffsetY = remember { Animatable(120f) }
    val glowPulse = remember { Animatable(0.4f) }
    val particleRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Parallel launch animations for dramatic 3D entrance
        launch {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(1200, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rotationY.animateTo(
                targetValue = 0f,
                animationSpec = tween(1400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rotationX.animateTo(
                targetValue = 0f,
                animationSpec = tween(1400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            launchOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            )
        }
        launch {
            glowPulse.animateTo(
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        launch {
            particleRotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing)
                )
            )
        }

        // Auto navigate after 3.2 seconds
        delay(3200)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TabahiOrangeLight,
                        TabahiOrange,
                        TabahiOrangeDark,
                        Color(0xFF871C04)
                    )
                )
            )
            .clickable { onSplashComplete() }
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        // Background 3D aura and radiating sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f - 60f)
            // Radiating rings
            drawCircle(
                color = TabahiGold.copy(alpha = 0.15f * glowPulse.value),
                radius = 240f + (30f * glowPulse.value),
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = 340f,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // 3D Container with custom perspective and tilt
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        this.scaleX = scale.value
                        this.scaleY = scale.value
                        this.rotationY = rotationY.value
                        this.rotationX = rotationX.value
                        this.translationY = launchOffsetY.value
                        this.cameraDistance = 16f * density
                    }
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(36.dp),
                        spotColor = TabahiAmber,
                        ambientColor = Color.Black
                    )
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF8F00),
                                Color(0xFFE65100),
                                Color(0xFFBF360C)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(300f, 300f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer gold metallic border
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent, TabahiGold.copy(alpha = 0.6f))
                        ),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(36.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Custom 3D Vector Icon: Pencil Rocket + Leaping Stickman + Golden Arrow
                Canvas(
                    modifier = Modifier
                        .size(190.dp)
                        .graphicsLayer {
                            this.rotationZ = (glowPulse.value - 0.7f) * 6f
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Pencil Rocket Base
                    val pencilPath = Path().apply {
                        moveTo(w * 0.46f, h * 0.66f)
                        lineTo(w * 0.54f, h * 0.66f)
                        lineTo(w * 0.50f, h * 0.82f)
                        close()
                    }
                    drawPath(
                        path = pencilPath,
                        color = Color(0xFFFFF8E1)
                    )
                    // Pencil wood collar
                    val collarPath = Path().apply {
                        moveTo(w * 0.475f, h * 0.73f)
                        lineTo(w * 0.525f, h * 0.73f)
                        lineTo(w * 0.50f, h * 0.82f)
                        close()
                    }
                    drawPath(path = collarPath, color = TabahiAmber)

                    // Graphite tip
                    val tipPath = Path().apply {
                        moveTo(w * 0.49f, h * 0.79f)
                        lineTo(w * 0.51f, h * 0.79f)
                        lineTo(w * 0.50f, h * 0.82f)
                        close()
                    }
                    drawPath(path = tipPath, color = Color(0xFF263238))

                    // Exhaust spark trail
                    drawLine(
                        color = TabahiGold,
                        start = Offset(w * 0.50f, h * 0.83f),
                        end = Offset(w * 0.47f, h * 0.89f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.50f, h * 0.83f),
                        end = Offset(w * 0.52f, h * 0.88f),
                        strokeWidth = 3f
                    )

                    // 2. Wings / Speed curves behind body
                    val wingPath = Path().apply {
                        moveTo(w * 0.48f, h * 0.32f)
                        cubicTo(w * 0.36f, h * 0.35f, w * 0.22f, h * 0.44f, w * 0.18f, h * 0.54f)
                        cubicTo(w * 0.28f, h * 0.48f, w * 0.38f, h * 0.45f, w * 0.46f, h * 0.44f)
                        close()
                    }
                    drawPath(path = wingPath, color = Color.White.copy(alpha = 0.9f))

                    // 3. Golden Upward Checkmark Arrow
                    val arrowPath = Path().apply {
                        moveTo(w * 0.56f, h * 0.48f)
                        lineTo(w * 0.65f, h * 0.40f)
                        lineTo(w * 0.72f, h * 0.46f)
                        lineTo(w * 0.86f, h * 0.24f)
                        lineTo(w * 0.74f, h * 0.26f)
                        lineTo(w * 0.78f, h * 0.31f)
                        lineTo(w * 0.65f, h * 0.40f)
                        close()
                    }
                    drawPath(
                        path = arrowPath,
                        brush = Brush.linearGradient(
                            listOf(TabahiGold, Color(0xFFFFB300), Color(0xFFFF8F00))
                        )
                    )

                    // 4. White Leaping Runner Stickman
                    // Head
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.075f,
                        center = Offset(w * 0.51f, h * 0.29f)
                    )

                    // Torso & dynamic launching legs
                    val bodyPath = Path().apply {
                        moveTo(w * 0.50f, h * 0.37f)
                        cubicTo(w * 0.53f, h * 0.38f, w * 0.56f, h * 0.40f, w * 0.57f, h * 0.43f)
                        lineTo(w * 0.69f, h * 0.34f) // Right arm raised up holding arrow
                        cubicTo(w * 0.71f, h * 0.33f, w * 0.73f, h * 0.36f, w * 0.71f, h * 0.38f)
                        lineTo(w * 0.60f, h * 0.47f)
                        lineTo(w * 0.54f, h * 0.55f)
                        lineTo(w * 0.64f, h * 0.56f) // Right leg kick
                        cubicTo(w * 0.67f, h * 0.57f, w * 0.67f, h * 0.61f, w * 0.64f, h * 0.63f)
                        lineTo(w * 0.53f, h * 0.73f)
                        lineTo(w * 0.49f, h * 0.61f)
                        lineTo(w * 0.37f, h * 0.77f) // Left trailing leg launched from pencil
                        cubicTo(w * 0.34f, h * 0.79f, w * 0.32f, h * 0.76f, w * 0.34f, h * 0.73f)
                        cubicTo(w * 0.41f, h * 0.63f, w * 0.45f, h * 0.52f, w * 0.48f, h * 0.45f)
                        lineTo(w * 0.36f, h * 0.49f) // Left arm counter-balancing
                        cubicTo(w * 0.33f, h * 0.50f, w * 0.32f, h * 0.47f, w * 0.34f, h * 0.45f)
                        cubicTo(w * 0.40f, h * 0.39f, w * 0.45f, h * 0.37f, w * 0.50f, h * 0.37f)
                        close()
                    }
                    drawPath(path = bodyPath, color = Color.White)

                    // Sparkle stars
                    drawCircle(color = TabahiGold, radius = 4f, center = Offset(w * 0.35f, h * 0.55f))
                    drawCircle(color = Color.White, radius = 5f, center = Offset(w * 0.70f, h * 0.48f))
                    drawCircle(color = TabahiGold, radius = 3.5f, center = Offset(w * 0.63f, h * 0.26f))
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Brand Typography with 3D Depth
            Text(
                text = "TABAHI",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = Color.White,
                modifier = Modifier
                    .shadow(elevation = 12.dp, shape = CircleShape, spotColor = Color.Black)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Peak Intelligence JEE & NEET CBT Prep",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TabahiGold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = TabahiGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "  100% Authentic PYQs • NTA CBT Simulator",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Action button to start immediately
            Button(
                onClick = onSplashComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = TabahiOrangeDark
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("splash_start_button")
            ) {
                Text(
                    text = "Launch Tabahi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Start"
                )
            }
        }
    }
}
