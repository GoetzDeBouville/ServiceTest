package com.hellcorp.servicetest

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        Log.d(LOG_TAG, "onCreate")
        mediaPlayer = MediaPlayer()
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy")

        mediaPlayer?.setOnPreparedListener(null)
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand | flags: $flags, startId: $startId")

        val songUrl = intent?.getStringExtra("song_url")
        if (songUrl != null) {
            Log.d(LOG_TAG, "onStartCommand -> song url exists")

            mediaPlayer?.setDataSource(songUrl)
            mediaPlayer?.prepareAsync()

            mediaPlayer?.setOnPreparedListener {
                it?.start()
            }

            mediaPlayer?.setOnCompletionListener {
                stopSelf()
            }
        }

        Log.d(LOG_TAG, "onStartCommand -> before return")

        return Service.START_NOT_STICKY
    }

    companion object {
        const val LOG_TAG = "MusicService"
    }
}