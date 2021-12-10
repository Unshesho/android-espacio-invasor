package com.shesho.espacioinvasor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shesho.espacioinvasor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding?.apply {
            setContentView(root)
        }
    }

    override fun onStart() {
        super.onStart()

        binding?.touchButton?.setOnClickListener {
            val intent = PlayableScreenActivity.makeIntent(this, false)
            startActivity(intent)
        }
        binding?.tiltButton?.setOnClickListener {
            val intent = PlayableScreenActivity.makeIntent(this, true)
            startActivity(intent)
        }
    }
}