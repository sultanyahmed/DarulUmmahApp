package com.example.darulummahapp

internal fun youtubeLiveEmbedUrl(channelId: String): String {
    return "https://www.youtube.com/embed/live_stream?channel=$channelId&playsinline=1&rel=0&origin=$YouTubeEmbedOrigin&widget_referrer=$YouTubeEmbedReferrer"
}

internal fun youtubeVideoEmbedUrl(videoId: String): String {
    return "https://www.youtube.com/embed/$videoId?playsinline=1&rel=0&origin=$YouTubeEmbedOrigin&widget_referrer=$YouTubeEmbedReferrer"
}

internal fun youtubeEmbedHtml(embedUrl: String): String {
    return """
        <!doctype html>
        <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta name="referrer" content="strict-origin-when-cross-origin">
                <style>
                    html, body, iframe {
                        width: 100%;
                        height: 100%;
                        margin: 0;
                        padding: 0;
                        background: #000;
                        overflow: hidden;
                    }
                </style>
            </head>
            <body>
                <iframe
                    src="$embedUrl"
                    title="Darul Ummah YouTube player"
                    frameborder="0"
                    referrerpolicy="strict-origin-when-cross-origin"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                    allowfullscreen>
                </iframe>
            </body>
        </html>
    """.trimIndent()
}

internal const val YouTubeEmbedBaseUrl = "https://www.darulummah.org.uk/"

private const val YouTubeEmbedOrigin = "https://www.darulummah.org.uk"
private const val YouTubeEmbedReferrer = "https%3A%2F%2Fwww.darulummah.org.uk%2F"
