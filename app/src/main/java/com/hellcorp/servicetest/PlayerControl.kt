package com.hellcorp.servicetest

import kotlinx.coroutines.flow.StateFlow

interface PlayerControl {
    fun getPlayerStateInternal(): StateFlow<PlayerState>
    fun startPlayer()
    fun pausePlayer()
}