package com.studios1299.playwall.explore.presentation

import com.studios1299.playwall.core.presentation.UiText

sealed interface ExploreEvent {
    data class NavigateToPhotoDetail(val initialPhotoIndex: Int): ExploreEvent
    data object ExitPhotoDetail: ExploreEvent
    data class ShowError(val error: UiText): ExploreEvent
}