package com.example.darulummahapp

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Base64
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal object AndroidAnnouncementImagePicker {
    private var launcher: ActivityResultLauncher<Array<String>>? = null
    private var continuation: CancellableContinuation<PickedAnnouncementImage?>? = null

    fun register(activity: ComponentActivity) {
        launcher = activity.registerForActivityResult(OpenDocument()) { uri ->
            val currentContinuation = continuation ?: return@registerForActivityResult
            continuation = null
            if (uri == null) {
                currentContinuation.resume(null)
                return@registerForActivityResult
            }
            activity.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            launchPickerDecode(
                currentContinuation = currentContinuation,
                resolver = activity.contentResolver,
                uri = uri,
            )
        }
    }

    suspend fun pickImage(): PickedAnnouncementImage? {
        val currentLauncher = launcher ?: error("Android announcement image picker is not registered.")
        return suspendCancellableCoroutine { cont ->
            continuation = cont
            currentLauncher.launch(arrayOf("image/*"))
            cont.invokeOnCancellation {
                if (continuation === cont) {
                    continuation = null
                }
            }
        }
    }
}

private fun launchPickerDecode(
    currentContinuation: CancellableContinuation<PickedAnnouncementImage?>,
    resolver: ContentResolver,
    uri: Uri,
) {
    CoroutineScope(Dispatchers.IO).launch {
        runCatching { decodePickedImage(resolver, uri) }
            .onSuccess { currentContinuation.resume(it) }
            .onFailure { currentContinuation.resumeWithException(it) }
    }
}

private fun decodePickedImage(
    resolver: ContentResolver,
    uri: Uri,
): PickedAnnouncementImage {
    val mimeType = resolver.getType(uri) ?: "image/jpeg"
    val fileName = queryDisplayName(resolver, uri) ?: "announcement-image"
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("Could not read the selected image.")
    return PickedAnnouncementImage(
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes,
    )
}

private fun queryDisplayName(
    resolver: ContentResolver,
    uri: Uri,
): String? {
    val cursor: Cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?: return null
    cursor.use {
        if (!it.moveToFirst()) return null
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index < 0) return null
        return it.getString(index)
    }
}

actual suspend fun pickAnnouncementImage(): PickedAnnouncementImage? {
    return AndroidAnnouncementImagePicker.pickImage()
}

actual fun encodeAnnouncementImageBase64(bytes: ByteArray): String {
    return Base64.getEncoder().encodeToString(bytes)
}

actual fun decodeAnnouncementImageBitmap(bytes: ByteArray): ImageBitmap? {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}
