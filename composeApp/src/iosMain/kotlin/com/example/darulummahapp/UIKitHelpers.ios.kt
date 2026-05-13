package com.example.darulummahapp

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

internal fun activeRootViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    return application.keyWindow?.rootViewController
        ?: application.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
            ?.rootViewController
        ?: application.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull()
            ?.rootViewController
}

internal fun topPresentedViewController(): UIViewController? {
    var controller = activeRootViewController()
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
