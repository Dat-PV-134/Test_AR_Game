package com.rekoj134.argamedemo

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import com.rekoj134.argamedemo.databinding.ActivityMainBinding
import android.Manifest
import android.content.Intent
import com.rekoj134.argamedemo.ar_game.ARGameActivity
import android.content.Context
import android.view.WindowManager
import android.view.Gravity
import android.graphics.PixelFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartGame.setOnClickListener {
            startActivity(Intent(this@MainActivity, ARGameActivity::class.java))
        }
    }
}