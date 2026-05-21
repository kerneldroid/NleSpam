package com.nlespam.models

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GlobalSignals {
    private val _stopSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val stopSignal = _stopSignal.asSharedFlow()

    fun sendStop() {
        _stopSignal.tryEmit(Unit)
    }
}
