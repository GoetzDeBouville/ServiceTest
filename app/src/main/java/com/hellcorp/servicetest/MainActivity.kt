package com.hellcorp.servicetest

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Если выдали разрешение — запускаем сервис.
            startMusicService()
        } else {
            // Иначе просто покажем ошибку
            Toast.makeText(this, "Can't start foreground service!", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startMusicServiceButton).setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                putExtra(SONG_URL_KEY, SONG_URL)
            }

            Log.i("MainActivity", "Button clicked intent = $intent")
            Log.i("MainActivity", "Process = ${android.os.Process.myPid()}")

            ContextCompat.startForegroundService(this, intent)
        }

        findViewById<Button>(R.id.startProgressServiceButton).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startMusicService()
            }
        }
    }

    private fun startMusicService() {
        val intent = Intent(this, MusicService::class.java).apply {
            putExtra(SONG_URL_KEY, SONG_URL)
        }

        ContextCompat.startForegroundService(this, intent)
    }

    companion object {
        const val SONG_URL = "https://files.freemusicarchive.org/storage-freemusicarchive-org/tracks/h3vL7veZhrT8ghGqKN0s02D0RFqOGPftD7fFtCga.mp3"
        const val SONG_URL_KEY = "song_url"
    }
}