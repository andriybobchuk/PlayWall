package com.studios1299.playwall.create.presentation

sealed class CreateScreenEvent {
    object ImageSaved : CreateScreenEvent()
    data class ShowError(val message: String) : CreateScreenEvent()
}

