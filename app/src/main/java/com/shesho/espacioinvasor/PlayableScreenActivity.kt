package com.shesho.espacioinvasor

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout.*
import androidx.core.animation.doOnEnd
import com.shesho.espacioinvasor.BulletConfig.ANIMATION_DISTANCE
import com.shesho.espacioinvasor.BulletConfig.ANIMATION_DURATION
import com.shesho.espacioinvasor.BulletConfig.HEIGHT
import com.shesho.espacioinvasor.BulletConfig.WIDTH
import com.shesho.espacioinvasor.databinding.ActivityPlayableScreenBinding

class PlayableScreenActivity : AppCompatActivity(), SensorEventListener {
    private var binding: ActivityPlayableScreenBinding? = null
    private var displayMetrics: DisplayMetrics? = null
    private var sensorManager: SensorManager? = null
    private var tiltControl = false
    private var laserSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayableScreenBinding.inflate(layoutInflater)

        binding?.apply {
            setContentView(root)
        }
    }

    override fun onStart() {
        super.onStart()

        setParameters()
        setControls()
    }

    private fun setParameters() {
        displayMetrics = DisplayMetrics()
        tiltControl = intent.getBooleanExtra(TILT_CONTROL, false)
        laserSound = MediaPlayer.create(this, R.raw.laser_sound)

        displayMetrics?.apply { windowManager.defaultDisplay.getMetrics(displayMetrics) }
    }

    private fun setControls() {
        setBulletControl()
        if (!tiltControl) clickControls()
        else setupAccelerometerSensorListener()
    }

    /* TODO: Change to automatic shooting */
    private fun setBulletControl() = binding?.apply {
        shootButton.setOnClickListener {
            shootBullet()
        }
    }

    private fun shootBullet() {
        val bullet = createBullet()
        playShootSound()
        translateBullet(bullet)
    }

    private fun createBullet(): ImageView {
        val bullet = ImageView(this)
        val params = LayoutParams(WIDTH, HEIGHT)
        bullet.layoutParams = params
        bullet.setBackgroundColor(resources.getColor(R.color.purple_500))
        bullet.x = getShipCenter() - bullet.width / 2
        binding?.apply {
            bullet.y = shipFrame.y
            container.addView(bullet)
        }
        return bullet
    }

    private fun getShipCenter(): Float {
        binding?.apply { return ship.x + ship.width.toFloat() / 2 }
        return 0f
    }

    private fun playShootSound() {
        if (laserSound?.isPlaying == true) laserSound?.seekTo(0)
        laserSound?.start()
    }

    private fun translateBullet(bullet: ImageView) {
        ObjectAnimator.ofFloat(bullet, "translationY", -ANIMATION_DISTANCE)
            .apply {
                duration = ANIMATION_DURATION
                doOnEnd { binding?.container?.removeView(bullet) }
                start()
            }
    }

    private fun clickControls() {
        binding?.apply {
            leftButton.setOnClickListener {
                val futureXLeft = ship.x - MOVEMENT_X
                if (futureXLeft >= 0) ship.x = futureXLeft
            }

            rightButton.setOnClickListener {
                val futureXRight = ship.x + MOVEMENT_X
                val limitRight = (displayMetrics?.widthPixels ?: 0) - ship.width
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
            val limitRight = (displayMetrics?.widthPixels ?: 0) - ship.width
            if (nextPosition >= 0 && nextPosition <= limitRight) ship.x = nextPosition
        }
    }

    private fun getSensorValues(event: SensorEvent): Float {
        if (event.sensor?.type == TYPE_ACCELEROMETER) {
            return event.values[0]
        }
        return 0f
    }

    override fun onStop() {
        super.onStop()
        displayMetrics = null
        laserSound = null
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