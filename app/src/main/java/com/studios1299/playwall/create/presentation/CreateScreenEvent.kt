package com.studios1299.playwall.create.presentation

sealed interface CreateScreenEvent {
    data object ImageSaved : CreateScreenEvent
    data class ShowError(val message: String) : CreateScreenEvent
}
