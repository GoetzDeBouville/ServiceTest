package com.hellcorp.servicetest

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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
    private var serviceJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")
        player = ExoPlayer.Builder(this).build()

        player?.addListener(playerListener)

        createNotificationChannel()

        ServiceCompat.startForeground(
            this,
            SERVICE_NOTIFICATION_ID,
            createServiceNotification(),
            getForegroundServiceTypeConstant()
        )
    }

    override fun onDestroy() {
        player?.release()
        serviceJob?.cancel()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand | flags: $flags, startId: $startId")

        val songUrl = intent?.getStringExtra(MainActivity.SONG_URL_KEY)
        if (songUrl != null) {
            Log.d(LOG_TAG, "onStartCommand -> song url exists")
            serviceJob = CoroutineScope(Dispatchers.Default).launch {
                prepareAndStartPlayer(songUrl)
            }
        }
        Log.i(LOG_TAG, "Process = ${android.os.Process.myPid()}")

        Log.d(LOG_TAG, "onStartCommand -> before return")
        return Service.START_REDELIVER_INTENT
    }

    private suspend fun prepareAndStartPlayer(songUrl: String) {
        withContext(Dispatchers.Main) {
            val mediaItem = MediaItem.fromUri(songUrl)
            player?.addMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                Log.d(LOG_TAG, "Player.STATE_ENDED")
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Music service",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Service for playing music"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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