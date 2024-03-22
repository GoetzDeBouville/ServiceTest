package com.hellcorp.servicetest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class MusicService : Service() {
    private var player: ExoPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")
        player = ExoPlayer.Builder(this).build()

        player?.addListener(playerListener)
    }

    override fun onDestroy() {
        player?.release()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand | flags: $flags, startId: $startId")

        val songUrl = intent?.getStringExtra(MainActivity.SONG_URL_KEY)
        if (songUrl != null) {
            Log.d(LOG_TAG, "onStartCommand -> song url exists")
            prepareAndStartPlayer(songUrl)
        }
        Log.i(LOG_TAG, "Process = ${android.os.Process.myPid()}")

        Log.d(LOG_TAG, "onStartCommand -> before return")
        return Service.START_NOT_STICKY
    }

    private fun prepareAndStartPlayer(songUrl: String) {
        val mediaItem = MediaItem.fromUri(songUrl)
        player?.addMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                Log.d(LOG_TAG, "Player.STATE_ENDED")
                stopSelf()
            }
        }
    }

    companion object {
        const val LOG_TAG = "MusicService"
    }
}