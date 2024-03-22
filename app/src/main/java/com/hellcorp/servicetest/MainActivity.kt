package com.hellcorp.servicetest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startMusicServiceButton).setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                putExtra(SONG_URL_KEY, SONG_URL)
            }

            Log.i("MainActivity", "Button clicked intent = $intent")
            Log.i("MainActivity", "Process = ${android.os.Process.myPid()}")
            startService(intent)
        }
    }

    companion object {
        const val SONG_URL = "https://files.freemusicarchive.org/storage-freemusicarchive-org/tracks/h3vL7veZhrT8ghGqKN0s02D0RFqOGPftD7fFtCga.mp3"
        const val SONG_URL_KEY = "song_url"
    }
}