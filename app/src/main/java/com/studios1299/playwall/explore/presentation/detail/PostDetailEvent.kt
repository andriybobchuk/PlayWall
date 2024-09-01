package com.studios1299.playwall.explore.presentation.detail

import com.studios1299.playwall.core.presentation.UiText

sealed interface PostDetailEvent {
    data object ExitImageDetail : PostDetailEvent
    data class ShowError(val error: UiText) : PostDetailEvent
}
