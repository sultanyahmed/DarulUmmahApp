package com.example.darulummahapp

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.random.Random
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IOSAnnouncementImagePickerDelegate(
    private val completion: (Result<PickedAnnouncementImage?>) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        completion(Result.success(null))
    }

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val image = (didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage)
            ?: (didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)
        if (image == null) {
            completion(Result.failure(IllegalStateException("Could not read the selected image.")))
            return
        }
        val imageData = UIImageJPEGRepresentation(image, 0.9)
        if (imageData == null) {
            completion(Result.failure(IllegalStateException("Could not encode the selected image.")))
            return
        }
        completion(
            Result.success(
                PickedAnnouncementImage(
                    fileName = "announcement-${Random.nextInt(100000, 999999)}.jpg",
                    mimeType = "image/jpeg",
                    bytes = imageData.toByteArray(),
                ),
            ),
        )
    }
}

private var activePickerDelegate: IOSAnnouncementImagePickerDelegate? = null

@OptIn(ExperimentalForeignApi::class)
actual suspend fun pickAnnouncementImage(): PickedAnnouncementImage? {
    val rootViewController = announcementPickerTopPresentedViewController()
        ?: error("Could not access the iOS root view controller.")

    return suspendCancellableCoroutine { continuation ->
        val picker = UIImagePickerController().apply {
            allowsEditing = false
        }
        val delegate = IOSAnnouncementImagePickerDelegate { result ->
            activePickerDelegate = null
            result
                .onSuccess { continuation.resume(it) }
                .onFailure { continuation.resumeWithException(it) }
        }
        activePickerDelegate = delegate
        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)
        continuation.invokeOnCancellation {
            activePickerDelegate = null
            picker.dismissViewControllerAnimated(true, completion = null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun encodeAnnouncementImageBase64(bytes: ByteArray): String {
    return bytes.toNSData().base64EncodedStringWithOptions(0u)
}

actual fun decodeAnnouncementImageBitmap(bytes: ByteArray): ImageBitmap? {
    return runCatching { Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size == 0) return result
    result.usePinned {
        memcpy(it.addressOf(0), bytes, length)
    }
    return result
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    return usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.toULong())
    }
}

private fun announcementPickerTopPresentedViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
