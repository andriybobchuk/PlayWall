package com.studios1299.playwall.core.presentation.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView


enum class WebContent(val title: String, val url: String) {
    TOS("Terms of Service", "https://www.facebook.com/terms.php?paipv=0&eav=AfYpS9BfUgSfKgu2rxjNFVIcBqsbTB9R63KFiuKDFqodvsFu401FYgmpPmrQi5pquFM&_rdr"),
    PP("Privacy Policy", "https://www.facebook.com/privacy/policy/version/7122790421067234"),
    CP("Content Policy", "https://www.instagram.com/"),
    FAQ("FAQ", "https://www.instagram.com/"),
    IG("Follow us on IG", "https://www.instagram.com/"),
    TIKTOK("Follow us on TikTok", "https://www.tiktok.com/"),
}

@Composable
fun WebViewScreen(policyType: WebContent, onBackClick: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }

    WebViewComponent(policyType.url, onBackClick, isLoading) {
        isLoading = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComponent(
    url: String,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    onPageFinished: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Toolbars.Primary(
                showBackButton = true,
                title = WebContent.entries.first { it.url == url }.title,
                onBackClick = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                onPageFinished() // Notify that the page has finished loading
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }
        }
    }
}
