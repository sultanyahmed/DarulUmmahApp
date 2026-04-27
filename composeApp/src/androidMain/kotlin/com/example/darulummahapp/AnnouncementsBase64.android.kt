package com.example.darulummahapp

import java.util.Base64

actual fun platformDecodeBase64(value: ByteArray): ByteArray {
    return Base64.getDecoder().decode(value)
}
