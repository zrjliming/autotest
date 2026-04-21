package com.cst.autotest

import android.app.UiAutomation
import android.content.Context
import android.view.WindowManager
import androidx.test.uiautomator.UiDevice
import java.io.File
import java.lang.ref.WeakReference

/**
 * Shared runtime holder populated by `UiAutomatorHelper` before the Ktor server starts.
 *
 * Request flow:
 * 1. `UiAutomatorHelper.setUp()` obtains instrumentation objects from Android test runtime.
 * 2. The helper writes those objects into this singleton.
 * 3. Ktor route classes read this singleton while handling HTTP requests.
 * 4. Click routes call `uiDevice.click(...)` directly or load a stored UI object from
 *    `objectStore` and invoke its click method.
 *
 * The normal app `Application` does not create these fields because it does not have access to
 * instrumentation-only objects such as `UiDevice` and `UiAutomation`.
 */
object Holder {
    /** Target app context wrapped weakly so route handlers can start activities and access resources. */
    lateinit var targetContext: WeakReference<Context>

    /** Window manager used by display-size related API routes. */
    lateinit var windowManager: WindowManager

    /** Cache directory used for temporary files such as screenshots and Ktor config files. */
    lateinit var cacheDir: File

    /** UiAutomator device instance used for global operations such as click, swipe, and key press. */
    lateinit var uiDevice: UiDevice

    /** UiAutomation instance used for lower-level Android automation APIs and event access. */
    lateinit var uiAutomation: UiAutomation

    /** Shared object id map for UI objects and wait conditions returned by API calls. */
    val objectStore: ObjectStore = ObjectStore()
}

// Didn't work, dagger was injecting different references even though this is annotated with
// singleton, as a workaround, HolderHolder was used
//@Singleton
//class Holder @Inject constructor() {
//    lateinit var targetContext: WeakReference<Context>
//    lateinit var windowManager: WindowManager
//    lateinit var cacheDir: File
//    lateinit var uiDevice: UiDevice
//}

class HolderHolder {
    /** Compatibility wrapper used by old migrated code paths that expected a holder provider. */
    val instance = Holder
}
