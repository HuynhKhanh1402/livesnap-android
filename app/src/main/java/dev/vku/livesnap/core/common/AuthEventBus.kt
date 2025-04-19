package dev.vku.livesnap.core.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun send(event: AuthEvent) {
        _events.tryEmit(event)
    }

    sealed class AuthEvent {
        object TokenExpired : AuthEvent()
    }
}