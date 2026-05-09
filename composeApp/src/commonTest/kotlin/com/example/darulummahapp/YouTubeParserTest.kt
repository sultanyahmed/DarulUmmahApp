package com.example.darulummahapp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class YouTubeParserTest {
    @Test
    fun channelVideosPageParserReadsRichItemRenderers() {
        val html = """
            <script>
                "richItemRenderer":{"content":{"lockupViewModel":{
                    "contentImage":{"thumbnailViewModel":{"image":{"sources":[{"url":"https://i.ytimg.com/vi/YKYMxJyl1fU/hqdefault.jpg"}]}}},
                    "metadata":{"lockupMetadataViewModel":{"title":{"content":"Shaykh Waleed al Mehsas | Surah Al-A'raf | Riwaayah Warsh an Nafi"}}},
                    "content":"12 days ago",
                    "watchEndpoint":{"videoId":"YKYMxJyl1fU"}
                }}}
                "richItemRenderer":{"content":{"lockupViewModel":{
                    "contentImage":{"thumbnailViewModel":{"image":{"sources":[{"url":"https://i.ytimg.com/vi/dlzN-7qZPbc/hqdefault.jpg"}]}}},
                    "metadata":{"lockupMetadataViewModel":{"title":{"content":"Qari Younus Rahman | Surah Ar - Ra'd | Riwaayah Warsh And Nafi'"}}},
                    "content":"2 weeks ago",
                    "watchEndpoint":{"videoId":"dlzN-7qZPbc"}
                }}}
            </script>
        """.trimIndent()

        val videos = parseYouTubeRecentVideosPage(html)

        assertEquals(2, videos.size)
        assertEquals("YKYMxJyl1fU", videos[0].id)
        assertEquals("Shaykh Waleed al Mehsas | Surah Al-A'raf | Riwaayah Warsh an Nafi", videos[0].title)
        assertEquals("12 days ago", videos[0].publishedDate)
        assertEquals("dlzN-7qZPbc", videos[1].id)
    }

    @Test
    fun liveParserReadsWatchCanonicalWhenChannelIsLive() {
        val html = """
            <html>
                <head>
                    <link rel="canonical" href="https://www.youtube.com/watch?v=live123abc">
                </head>
            </html>
        """.trimIndent()

        assertEquals("live123abc", parseYouTubeLiveVideoId(html))
    }

    @Test
    fun liveParserIgnoresChannelCanonicalWhenChannelIsOffline() {
        val html = """
            <html>
                <head>
                    <link rel="canonical" href="https://www.youtube.com/channel/UCy7hFfaw0R-z8Mpg4zwMJrA">
                </head>
            </html>
        """.trimIndent()

        assertNull(parseYouTubeLiveVideoId(html))
    }

    @Test
    fun liveParserReadsLiveVideoRenderer() {
        val html = """
            <script>
                "videoRenderer":{
                    "videoId":"liveRenderer123",
                    "thumbnailOverlays":[{"thumbnailOverlayTimeStatusRenderer":{"style":"LIVE"}}]
                }
            </script>
        """.trimIndent()

        assertEquals("liveRenderer123", parseYouTubeLiveVideoId(html))
    }
}
