package com.studios1299.playwall.core.presentation.components

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.studios1299.playwall.R

object Images {

    private const val LOG_TAG = "Images"

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun Circle(
        modifier: Modifier = Modifier,
        model: Any?,
        size: Dp = 40.dp,
        onClick: () -> Unit = {}
    ) {
        var imageLoadFailed by remember(model) { mutableStateOf(false) }

        if (imageLoadFailed) {
            Box(
                modifier = modifier
                    .width(size)
                    .height(size)
                    .clip(CircleShape)
                    .clickable { onClick() }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxSize(0.6f)
                        .align(Alignment.Center)
                )
            }
        } else {
            GlideImage(
                model = model,
                contentDescription = stringResource(R.string.image),
                modifier = modifier
                    .width(size)
                    .height(size)
                    .clip(CircleShape)
                    .clickable { onClick() }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop,
                requestBuilderTransform = { requestBuilder ->
                    requestBuilder.addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(LOG_TAG, "Image load failed: ${model}")
                            imageLoadFailed = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.v(LOG_TAG, "Image loaded successfully: ${model}")
                            imageLoadFailed = false
                            return false
                        }
                    })
                }
            )
        }
    }
}