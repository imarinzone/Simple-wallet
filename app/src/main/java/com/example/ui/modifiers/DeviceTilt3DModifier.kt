package com.example.ui.modifiers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Normalized 3D tilt coordinates derived from real hardware sensors.
 * pitch: -1f (tilted forward) to +1f (tilted backward)
 * roll:  -1f (tilted left) to +1f (tilted right)
 */
@Immutable
data class DeviceTiltState(
    val pitch: Float = 0f,
    val roll: Float = 0f
)

/**
 * Connects to Android hardware orientation sensors (Rotation Vector / Accelerometer)
 * to stream smooth real-time physical device tilt.
 */
@Composable
fun rememberDeviceTiltState(
    enabled: Boolean = true,
    smoothingFactor: Float = 0.15f
): State<DeviceTiltState> {
    val context = LocalContext.current
    val tiltState = remember { mutableStateOf(DeviceTiltState()) }

    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensorManager == null || rotationSensor == null) {
            return@DisposableEffect onDispose {}
        }

        var smoothedPitch = 0f
        var smoothedRoll = 0f

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    // Pitch (radians) is orientationAngles[1], Roll (radians) is orientationAngles[2]
                    val rawPitch = (orientationAngles[1] * (180f / Math.PI.toFloat())).coerceIn(-45f, 45f) / 45f
                    val rawRoll = (orientationAngles[2] * (180f / Math.PI.toFloat())).coerceIn(-45f, 45f) / 45f

                    smoothedPitch += (rawPitch - smoothedPitch) * smoothingFactor
                    smoothedRoll += (rawRoll - smoothedRoll) * smoothingFactor

                    tiltState.value = DeviceTiltState(
                        pitch = smoothedPitch,
                        roll = smoothedRoll
                    )
                } else {
                    // Accelerometer / Gravity fallback
                    val normX = (event.values[0] / 9.8f).coerceIn(-1f, 1f)
                    val normY = (event.values[1] / 9.8f).coerceIn(-1f, 1f)

                    smoothedRoll += (-normX - smoothedRoll) * smoothingFactor
                    smoothedPitch += (normY - smoothedPitch) * smoothingFactor

                    tiltState.value = DeviceTiltState(
                        pitch = smoothedPitch,
                        roll = smoothedRoll
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            rotationSensor,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return tiltState
}

/**
 * Custom Compose Modifier that dynamically tilts elements in 3D perspective
 * and renders a responsive holographic specular shine across the card surface
 * driven by both real-time device tilt sensors and a smooth continuous shimmer sweep.
 */
fun Modifier.sensor3DTilt(
    tiltState: DeviceTiltState,
    maxTiltDegrees: Float = 14f,
    baseElevation: Dp = 0.dp,
    shadowColor: Color = Color.Transparent,
    cornerRadius: Dp = 18.dp
): Modifier = composed {
    val density = LocalDensity.current

    // Continuous smooth holographic shine sweep so the card shines even when stationary
    val infiniteTransition = rememberInfiniteTransition(label = "cardShineTransition")
    val ambientSweep by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientShineSweep"
    )

    // Spring interpolation for fluid physics responsiveness
    val animatedPitch by animateFloatAsState(
        targetValue = tiltState.pitch,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pitchSpring"
    )

    val animatedRoll by animateFloatAsState(
        targetValue = tiltState.roll,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rollSpring"
    )

    val rotX = -animatedPitch * maxTiltDegrees
    val rotY = animatedRoll * maxTiltDegrees
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    this
        .graphicsLayer {
            rotationX = rotX
            rotationY = rotY
            cameraDistance = 16f * density.density
            shadowElevation = 0f
        }
        .drawWithContent {
            drawContent()

            // Dynamic specular holographic reflection shifting across card surface
            val lightCenterNormX = 0.5f + (animatedRoll * 0.4f) + ambientSweep
            val lightCenterNormY = 0.5f - (animatedPitch * 0.4f) - (ambientSweep * 0.5f)

            val sheenBrush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0f),
                    Color.White.copy(alpha = 0.20f),
                    Color(0xFFE5A93C).copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0f)
                ),
                start = Offset(size.width * (lightCenterNormX - 0.38f), size.height * (lightCenterNormY - 0.38f)),
                end = Offset(size.width * (lightCenterNormX + 0.38f), size.height * (lightCenterNormY + 0.38f))
            )

            drawRoundRect(
                brush = sheenBrush,
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                blendMode = BlendMode.Screen
            )
        }
}
