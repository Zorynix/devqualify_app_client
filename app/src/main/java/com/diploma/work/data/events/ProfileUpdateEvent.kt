package com.diploma.work.data.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class ProfileEvent {
    object TestCompleted : ProfileEvent()
    object ProfileUpdated : ProfileEvent()
}

@Singleton
class ProfileEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()
    
    suspend fun emitEvent(event: ProfileEvent) {
        _events.emit(event)
    }
}
