package com.studios1299.playwall.explore.presentation.explore

import com.studios1299.playwall.core.presentation.UiText

sealed interface ExploreEvent {
    data class NavigateToPhotoDetail(val initialPhotoIndex: String): ExploreEvent
    data class ShowError(val error: UiText): ExploreEvent
}