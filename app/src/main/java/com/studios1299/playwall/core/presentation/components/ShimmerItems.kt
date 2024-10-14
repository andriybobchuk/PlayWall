package com.studios1299.playwall.core.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerFriendListItem() {
    Log.e("Shimmering", "...")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle for friend's profile picture
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Rectangle representing name
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rectangle representing subtitle (e.g., status)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            )
        }
    }
}

@Composable
fun ShimmerUserProfile() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle for profile picture
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Rectangle for user's name
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rectangle for user's bio/status
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray)
        )
    }
}

@Composable
fun ShimmerMessageItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Circle for message sender's profile picture
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Rectangle for message sender's name
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rectangle for message content preview
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            )
        }
    }
}

@Composable
fun ShimmerLoadingForFriendsList(
    modifier: Modifier
) {
    Column(modifier = modifier.shimmer()) {
        repeat(5) {
            ShimmerFriendListItem()
        }
    }
}

@Composable
fun ShimmerLoadingForUserProfile() {
    Column(modifier = Modifier.shimmer()) {
        ShimmerUserProfile()
    }
}

@Composable
fun ShimmerLoadingForWallpaperGrid() {
    // Wrap content in a LazyVerticalGrid for better performance
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columns
        modifier = Modifier
            .shimmer() // Apply shimmer effect
            .padding(4.dp),
        contentPadding = PaddingValues(4.dp), // Add padding around the grid
        horizontalArrangement = Arrangement.spacedBy(4.dp), // Space between columns
        verticalArrangement = Arrangement.spacedBy(4.dp) // Space between rows
    ) {
        items(18) { // 18 grid items
            ShimmerWallpaperGridItem()
        }
    }
}

@Composable
fun ShimmerWallpaperGridItem() {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Maintain 1:1 aspect ratio for square items
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Gray) // Placeholder color
    )
}


@Composable
fun ShimmerLoadingForMessages() {
    Column(modifier = Modifier.shimmer()) {
        repeat(5) {
            ShimmerMessageItem()
        }
    }
}
