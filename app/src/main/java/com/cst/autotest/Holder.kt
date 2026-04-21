package com.cst.autotest

import android.app.UiAutomation
import android.content.Context
import android.view.WindowManager
import androidx.test.uiautomator.UiDevice
import java.io.File
import java.lang.ref.WeakReference

/**
 * Ktor 服务启动前由 `UiAutomatorHelper` 填充的运行时共享容器。
 *
 * 请求流程：
 * 1. `UiAutomatorHelper.setUp()` 从 Android 测试运行时获取 instrumentation 对象。
 * 2. helper 将这些对象写入这个单例。
 * 3. Ktor 路由处理 HTTP 请求时读取这个单例。
 * 4. 点击路由会直接调用 `uiDevice.click(...)`，或从 `objectStore` 取出已保存的控件对象后调用它的点击方法。
 *
 * 普通应用的 `Application` 无法创建这些字段，因为它拿不到 `UiDevice`、`UiAutomation`
 * 这类只存在于 instrumentation 环境中的对象。
 */
object Holder {
    /** 目标应用的上下文，使用弱引用保存，供路由启动 Activity 或访问资源。 */
    lateinit var targetContext: WeakReference<Context>

    /** 窗口管理器，供屏幕尺寸相关接口读取显示信息。 */
    lateinit var windowManager: WindowManager

    /** 缓存目录，用于保存截图、Ktor 配置等临时文件。 */
    lateinit var cacheDir: File

    /** UiAutomator 的设备实例，用于点击、滑动、按键等全局操作。 */
    lateinit var uiDevice: UiDevice

    /** UiAutomation 实例，用于更底层的 Android 自动化接口和事件访问。 */
    lateinit var uiAutomation: UiAutomation

    /** 接口返回的 UI 对象和等待条件会保存在这里，并通过对象 id 再次访问。 */
    val objectStore: ObjectStore = ObjectStore()
}

// Dagger 即使标记为单例也会注入不同引用，因此保留 HolderHolder 作为兼容方案。
//@Singleton
//class Holder @Inject constructor() {
//    lateinit var targetContext: WeakReference<Context>
//    lateinit var windowManager: WindowManager
//    lateinit var cacheDir: File
//    lateinit var uiDevice: UiDevice
//}

class HolderHolder {
    /** 兼容旧迁移代码的包装类，旧代码仍按 holder provider 的方式获取 Holder。 */
    val instance = Holder
}
