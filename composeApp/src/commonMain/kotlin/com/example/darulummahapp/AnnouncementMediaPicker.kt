package com.example.darulummahapp

import androidx.compose.ui.graphics.ImageBitmap

data class PickedAnnouncementImage(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)

expect suspend fun pickAnnouncementImage(): PickedAnnouncementImage?

expect fun encodeAnnouncementImageBase64(bytes: ByteArray): String

expect fun decodeAnnouncementImageBitmap(bytes: ByteArray): ImageBitmap?
