package com.studios1299.playwall.core.presentation.components

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
                        Log.e(LOG_TAG, "Image load failed", e)
                        e?.logRootCauses(LOG_TAG)
                        e?.causes?.forEach { cause ->
                            Log.e(LOG_TAG, "Cause: ${cause.message}", cause)
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.i(LOG_TAG, "Image loaded successfully")
                        return false
                    }
                })
            }
        )
    }
}