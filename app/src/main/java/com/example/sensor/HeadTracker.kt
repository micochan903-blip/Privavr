package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI

data class HeadOrientation(
    val pitch: Float = 0f, // Up / Down tilt in degrees (-90 to +90)
    val yaw: Float = 0f,   // Left / Right turn in degrees (-180 to +180)
    val roll: Float = 0f   // Ear to shoulder tilt in degrees
)

class HeadTracker(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _orientationFlow = MutableStateFlow(HeadOrientation())
    val orientationFlow: StateFlow<HeadOrientation> = _orientationFlow.asStateFlow()

    private var initialYaw: Float = 0f
    private var initialPitch: Float = 0f
    private var hasCalibrated = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    fun startTracking() {
        rotationSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    fun recenter() {
        val current = _orientationFlow.value
        initialYaw += current.yaw
        initialPitch += current.pitch
        _orientationFlow.value = HeadOrientation(pitch = 0f, yaw = 0f, roll = current.roll)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
            event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
        ) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // orientationAngles: [0] = azimuth (yaw), [1] = pitch, [2] = roll (in radians)
            val rawYaw = (orientationAngles[0] * (180f / PI.toFloat()))
            val rawPitch = (orientationAngles[1] * (180f / PI.toFloat()))
            val rawRoll = (orientationAngles[2] * (180f / PI.toFloat()))

            if (!hasCalibrated) {
                initialYaw = rawYaw
                initialPitch = rawPitch
                hasCalibrated = true
            }

            var adjustedYaw = rawYaw - initialYaw
            var adjustedPitch = rawPitch - initialPitch

            // Normalize yaw between -180 and 180
            while (adjustedYaw > 180f) adjustedYaw -= 360f
            while (adjustedYaw < -180f) adjustedYaw += 360f

            // Smooth damping
            val current = _orientationFlow.value
            val alpha = 0.25f // Low-pass filter for smooth VR viewing without jitter
            val smoothYaw = current.yaw + alpha * (adjustedYaw - current.yaw)
            val smoothPitch = current.pitch + alpha * (adjustedPitch - current.pitch)
            val smoothRoll = current.roll + alpha * (rawRoll - current.roll)

            _orientationFlow.value = HeadOrientation(
                pitch = smoothPitch.coerceIn(-60f, 60f),
                yaw = smoothYaw.coerceIn(-120f, 120f),
                roll = smoothRoll.coerceIn(-45f, 45f)
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
