package com.example.darulummahapp

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

internal fun activeRootViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val keyWindow = application.keyWindow
    return keyWindow?.rootViewController
}

internal fun topPresentedViewController(): UIViewController? {
    var controller = activeRootViewController()
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
