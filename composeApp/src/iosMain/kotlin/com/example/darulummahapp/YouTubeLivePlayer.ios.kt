package com.example.darulummahapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.Foundation.NSURL
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
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

@OptIn(ExperimentalForeignApi::class)
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

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun YouTubeWebPlayer(
    url: String,
    modifier: Modifier,
) {
    var loadedUrl by remember { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<WKWebView?>(null) }

    DisposableEffect(url) {
        onDispose {
            player?.stopLoading()
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = platform.CoreGraphics.CGRectZero.readValue(),
                configuration = WKWebViewConfiguration().apply {
                    allowsInlineMediaPlayback = true
                    mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
                    preferences.javaScriptCanOpenWindowsAutomatically = true
                },
            ).apply {
                scrollView.scrollEnabled = false
                scrollView.bounces = false
                allowsBackForwardNavigationGestures = false
                opaque = false
            }
        },
        update = { webView ->
            player = webView
            if (loadedUrl != url) {
                loadedUrl = url
                webView.loadHTMLString(
                    string = youtubeEmbedHtml(url),
                    baseURL = NSURL.URLWithString(YouTubeEmbedBaseUrl),
                )
            }
        },
    )
}
