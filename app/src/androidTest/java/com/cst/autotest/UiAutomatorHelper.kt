package com.cst.autotest

import android.app.Instrumentation
import android.content.Context.WINDOW_SERVICE
import android.view.WindowManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import io.ktor.application.*
import io.ktor.locations.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.ref.WeakReference

/**
 * The port.
 */
private const val PORT = 9987

@KtorExperimentalLocationsAPI
@RunWith(AndroidJUnit4::class)
class UiAutomatorHelper {
    private lateinit var instrumentation: Instrumentation

    @Before
    fun setUp() {
        // This is the start of the runtime wiring for every HTTP request.
        // The Ktor server runs inside this instrumentation process, so every route can later
        // read Holder.uiDevice and Holder.uiAutomation when it needs to click or inspect UI.
        instrumentation = InstrumentationRegistry.getInstrumentation()

        val holder = Holder
        holder.targetContext = WeakReference(instrumentation.targetContext)
        holder.uiDevice = UiDevice.getInstance(instrumentation)
        holder.cacheDir = instrumentation.targetContext.cacheDir
        holder.windowManager =
            instrumentation.targetContext.getSystemService(WINDOW_SERVICE) as WindowManager
        holder.uiAutomation = instrumentation.uiAutomation
    }

    @Test
    fun uiAutomatorHelper() {
        val ktorApplicationConfigurationFile = createKtorApplicationConfigurationFile()
        val args = arrayOf("-config=${ktorApplicationConfigurationFile!!.absolutePath}")
        io.ktor.server.netty.EngineMain.main(args)
    }

    /**
     * Creates a temporary file containing the configuration.
     */
    private fun createKtorApplicationConfigurationFile(): File? {
        val tempDir = instrumentation.targetContext.cacheDir
        val tempFile = File.createTempFile("ktor-application", "conf", tempDir)
        val applicationConfiguration = "ktor {\n" +
                "    deployment {\n" +
                "        port = ${PORT}\n" +
                "        port = \${?PORT}\n" +
                "\n" +
                "        shutdown.url = \"/ktor/application/shutdown\"\n" +
                "    }\n" +
                "    application {\n" +
                "        modules = [ com.cst.autotest.KtorApplicationKt.module ]\n" +
                "    }\n" +
                "}"
        tempFile.writeText(applicationConfiguration)
        return tempFile
    }

    @After
    fun tearDown() {
        // TODO: should stop ktor server here
    }

    /**
     * Quits the server.
     */
    fun quit() {
        // TODO: should stop ktor server here
    }

    // WARNING:
    // won't be found because this is inside the test application code and not the main app
    @Suppress("unused")
    fun Application.module(testing: Boolean = false) {
    }
}
