package com.shesho.espacioinvasor

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.shesho.espacioinvasor.databinding.ActivityPlayableScreenBinding

class PlayableScreenActivity : AppCompatActivity(), SensorEventListener {
    private var binding: ActivityPlayableScreenBinding? = null
    private val metrics = DisplayMetrics()
    private var sensorManager: SensorManager? = null
    private var tiltControl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayableScreenBinding.inflate(layoutInflater)

        binding?.apply {
            setContentView(root)
        }

        getControls()
    }

    private fun getControls() {
        tiltControl = intent.getBooleanExtra(TILT_CONTROL, false)
    }

    override fun onStart() {
        super.onStart()

        windowManager.defaultDisplay.getMetrics(metrics)

        if (!tiltControl) clickControls()
        else setupAccelerometerSensorListener()
    }

    private fun clickControls() {
        binding?.apply {
            leftButton.setOnClickListener {
                val futureXLeft = ship.x - MOVEMENT_X
                if (futureXLeft >= 0) ship.x = futureXLeft
            }

            rightButton.setOnClickListener {
                val futureXRight = ship.x + MOVEMENT_X
                val limitRight = metrics.widthPixels - ship.width
                if (futureXRight <= limitRight) ship.x = futureXRight
            }
        }
    }

    private fun setupAccelerometerSensorListener() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager?.getDefaultSensor(TYPE_ACCELEROMETER)?.also { sensor ->
            sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.apply {
            tiltControl(event)
        }
    }

    private fun tiltControl(event: SensorEvent) {
        val xRotation = getSensorValues(event)
        binding?.apply {
            val nextPosition = ship.x - xRotation
            val limitRight = metrics.widthPixels - ship.width
            if (nextPosition >= 0 && nextPosition <= limitRight) ship.x = nextPosition
        }
    }

    private fun getSensorValues(event: SensorEvent): Float {
        if (event.sensor?.type == TYPE_ACCELEROMETER) {
            return event.values[0]
        }
        return 0f
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    companion object {
        const val MOVEMENT_X = 60
        const val TILT_CONTROL = "tilt_control"

        fun makeIntent(context: Context, useTiltControl: Boolean): Intent {
            return Intent(context, PlayableScreenActivity::class.java).also { intent ->
                intent.putExtra(TILT_CONTROL, useTiltControl)
            }
        }
    }
}