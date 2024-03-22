package com.hellcorp.servicetest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.concurrent.Executors

class ProgressService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        Executors.newSingleThreadExecutor().execute {

            for (i in 0..100) {
                Log.i(LOG_TAG, "Progress: $i%")
                Thread.sleep(333)
            }
            stopSelf()

        }

        return Service.START_NOT_STICKY
    }

    companion object {
        const val LOG_TAG = "MusicService"
    }
}