package com.studios1299.playwall.explore.presentation.explore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState
import com.studios1299.playwall.feature.play.presentation.play.Friend

data class ExploreState(
    override val wallpapers: List<ExploreWallpaper> = emptyList(),
    override val isLoading: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val currentPhotoIndex: Int = 0,
) : ImageGridState()

//object ExploreStateSingleton {
//    var state: ExploreState = ExploreState()
//
//    fun updateState(newState: ExploreState) {
//        state = newState
//    }
//}

object ExploreStateSingleton {
    var state: ExploreState by mutableStateOf(ExploreState())

    fun updateState(newState: ExploreState) {
        state = newState
    }
}



data class ExploreWallpaper(
    val id: Int,
    val fileName: String,
    val type: String,
    val sentCount: Int,
    val savedCount: Int,
    val isLiked: Boolean,
    val dateCreated: String,
)
