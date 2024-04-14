package com.hellcorp.servicetest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default())
    val plpayerState: StateFlow<PlayerState>
        get() = _playerState

    private var playerControl: PlayerControl? = null

    override fun onCleared() {
        super.onCleared()
        removePlayerControl()
    }

    fun playerControlManager(playerControl: PlayerControl) {
        this.playerControl = playerControl
        viewModelScope.launch {
            playerControl.getPlayerStateInternal().collect {
                _playerState.value = it
            }
        }
    }

    fun onPlayerButtonCLicked() {
        if (_playerState.value is PlayerState.Playing) {
            playerControl?.pausePlayer()
        } else {
            playerControl?.startPlayer()
        }
    }

    fun removePlayerControl() {
        playerControl = null
    }
}