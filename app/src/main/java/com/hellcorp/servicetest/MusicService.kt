package com.hellcorp.servicetest

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MusicService : Service() {
    private var player: ExoPlayer? = null
    private var songUrl = ""
    private val binder = MusicServiceBinder()

    //    private var playerState: PlayerState = PlayerState.Default()
    private var timerJob: Job? = null

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default())
    val playerState: StateFlow<PlayerState>
        get() = _playerState

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        songUrl = intent?.getStringExtra(MainActivity.SONG_URL_KEY) ?: ""
        if (songUrl.isNotEmpty()) {
            preparePlayer(songUrl)
        }

        ServiceCompat.startForeground(
            this,
            SERVICE_NOTIFICATION_ID,
            createServiceNotification(),
            getForegroundServiceTypeConstant()
        )
        return binder
    }

    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (player?.isPlaying == true) {
                delay(30L)
                _playerState.value = PlayerState.Playing(getCurrentPlayerPosition())
            }
        }
    }

    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(player?.currentPosition)
            ?: "00:00"
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player?.addListener(playerListener)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        releasePlayer()
        stopSelf()
        return super.onUnbind(intent)
    }

    private fun preparePlayer(songUrl: String) {
        val mediaItem = MediaItem.fromUri(songUrl)
        player?.addMediaItem(mediaItem)
        player?.prepare()
    }

    fun playPlayer() {
        player?.play()
        _playerState.value = PlayerState.Playing(getCurrentPlayerPosition())
        startTimer()
    }

    fun pausePlayer() {
        player?.pause()
        timerJob?.cancel()
        _playerState.value = PlayerState.Paused(getCurrentPlayerPosition())
    }

    private fun releasePlayer() {
        player?.stop()
        player?.release()
        timerJob?.cancel()
        _playerState.value = PlayerState.Default()
        player = null
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED || state == Player.STATE_READY) {
                _playerState.value = PlayerState.Prepared()
            } else if (state == Player.STATE_BUFFERING) {
                _playerState.value = PlayerState.Loading()
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
        const val NOTIFICATION_CHANNEL_ID = "music_service_channel"
        const val SERVICE_NOTIFICATION_ID = 100
    }
}