package com.example.darulummahapp

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun YouTubeLivePlayer(
    channelId: String,
    modifier: Modifier,
) {
    YouTubeWebPlayer(
        url = youtubeLiveEmbedUrl(channelId),
        modifier = modifier,
    )
}

@Composable
actual fun YouTubeVideoPlayer(
    videoId: String,
    modifier: Modifier,
) {
    YouTubeWebPlayer(
        url = youtubeVideoEmbedUrl(videoId),
        modifier = modifier,
    )
}

@Composable
private fun YouTubeWebPlayer(
    url: String,
    modifier: Modifier,
) {
    var player: WebView? = null
    DisposableEffect(url) {
        onDispose {
            player?.stopLoading()
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadsImagesAutomatically = true
            }
        },
        update = { webView ->
            player = webView
            if (webView.tag != url) {
                webView.tag = url
                webView.loadDataWithBaseURL(
                    YouTubeEmbedBaseUrl,
                    youtubeEmbedHtml(url),
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        },
    )
}
