package com.example.darulummahapp

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.initWithBase64EncodedData
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun platformDecodeBase64(value: ByteArray): ByteArray {
    val encodedData = value.usePinned {
        NSData.create(bytes = it.addressOf(0), length = value.size.toULong())
    }
    val decodedData = NSData.create(base64EncodedData = encodedData, options = 0u)
        ?: error("Could not decode the selected image.")
    val result = ByteArray(decodedData.length.toInt())
    if (result.isEmpty()) return result
    result.usePinned {
        memcpy(it.addressOf(0), decodedData.bytes, decodedData.length)
    }
    return result
}
