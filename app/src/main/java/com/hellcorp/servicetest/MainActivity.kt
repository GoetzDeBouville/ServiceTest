package com.hellcorp.servicetest

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var musicService: MusicService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicServiceBinder
            musicService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Если выдали разрешение — привязываемся к сервису.
            bindMusicService()
        } else {
            // Иначе просто покажем ошибку
            Toast.makeText(this, "Can't bind service!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Прежде чем привязаться к сервису, который будет показывать уведомление
        // Мы должны проверить, выданы ли соответствующие разрешения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // На версиях ниже Android 13 —
            // можно сразу привязаться к сервису.
            bindMusicService()
        }

        findViewById<Button>(R.id.playButton).setOnClickListener {
            musicService?.playPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindMusicService()
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java).putExtra(
            SONG_URL_KEY, SONG_URL
        )
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unBindMusicService() {
        unbindService(serviceConnection)
    }

    companion object {
        const val SONG_URL =
            "https://files.freemusicarchive.org/storage-freemusicarchive-org/tracks/h3vL7veZhrT8ghGqKN0s02D0RFqOGPftD7fFtCga.mp3"
        const val SONG_URL_KEY = "song_url"
    }
}