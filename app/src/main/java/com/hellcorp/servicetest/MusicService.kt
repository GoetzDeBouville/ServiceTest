package com.hellcorp.servicetest

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicService : Service() {
    private var player: ExoPlayer? = null
    private var songUrl = ""
    private val binder = MusicServiceBinder()

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        songUrl = intent?.getStringExtra(MainActivity.SONG_URL_KEY) ?: ""
        if (songUrl.isNotEmpty()) {
            prepareAndStartPlayer(songUrl)
        }

        ServiceCompat.startForeground(
            this,
            SERVICE_NOTIFICATION_ID,
            createServiceNotification(),
            getForegroundServiceTypeConstant()
        )
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player?.addListener(playerListener)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player?.release()
        stopSelf()
        return super.onUnbind(intent)
    }

    private fun prepareAndStartPlayer(songUrl: String) {
        val mediaItem = MediaItem.fromUri(songUrl)
        player?.addMediaItem(mediaItem)
        player?.prepare()
    }

    fun playPlayer() {
        player?.play()
    }

    fun pausePlayer() {
        player?.pause()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                Log.d(LOG_TAG, "Player.STATE_ENDED")
            }
        }
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Music foreground service")
            .setContentText("Our service level is working right now")
            .setSmallIcon(R.drawable.baseline_child_care_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun getForegroundServiceTypeConstant(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
    }

    companion object {
        const val LOG_TAG = "MusicService"
        const val NOTIFICATION_CHANNEL_ID = "music_service_channel"
        const val SERVICE_NOTIFICATION_ID = 100
    }
}