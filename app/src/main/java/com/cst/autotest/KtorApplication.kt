package com.cst.autotest

import com.cst.autotest.location.*
import com.cst.autotest.location.ObjectStore
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.withCharset
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ShutDownUrl
import io.ktor.swagger.experimental.HttpException
import io.swagger.server.models.ClickBody
import io.swagger.server.models.Locale
import io.swagger.server.models.PerformTwoPointerGestureBody
import io.swagger.server.models.Selector
import io.swagger.server.models.SwipeBody
import io.swagger.server.models.Text
import java.io.File

/**
 * Ktor 服务的主入口，负责安装通用插件并注册 `/v2` 下的自动化接口路由。
 *
 * 从收到 HTTP 请求到点击按钮的大致链路：
 * 1. Ktor 根据 URL 匹配这里注册的 Location 路由。
 * 2. 路由对象的 `response()` 方法读取参数或请求体。
 * 3. `response()` 通过 `Holder` 获取 UiAutomator 运行时对象。
 * 4. 坐标点击直接调用 `Holder.uiDevice.click(...)`；控件点击先用 oid 从
 *    `Holder.objectStore` 找到按钮对象，再调用对应控件的 `click()`。
 */
@KtorExperimentalLocationsAPI
@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val shutdownUrl = "/quit"

    install(CallLogging) {
    }

    install(Authentication) {
    }

    // 客户端支持压缩时，这个插件会自动压缩响应。
    install(Compression) {
    }

    // 允许跨域访问，方便外部客户端直接调用测试接口。
    install(CORS) {
        anyHost()
    }

    install(ContentNegotiation) {
        gson {
        }

        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Locations) {
    }

    install(ShutDownUrl.ApplicationCallFeature) {
        // 被拦截的关闭地址，也可以通过 application.conf 的 ktor.deployment.shutdown.url 配置。
        shutDownUrl = shutdownUrl
        // 返回进程退出码的函数，类型是 ApplicationCall.() -> Int。
        exitCodeSupplier = { 0 }
    }

    routing {
        trace {
            application.log.trace(it.buildText())
            println("🚜")
            println(it.buildText())
        }

        get("/") {
            val local = call.request.local
            val scheme = local.scheme
            val host = local.host
            val port = local.port
            call.respondText(
                "CulebraTester2: Go to ${scheme}://${host}:${port}/help for usage details.\n",
                contentType = ContentType.Text.Plain
            )
        }

        // 静态资源路由，例如可以访问 `/static/ktor_logo.svg`。
        static("/static") {
            resources("static")
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }

        get<Help> {
            call.respond(it.response())
        }

        route("/v2") {

            get<Culebra.Info> {
                call.respond(it.response())
            }

            get<Culebra.Help> {
                call.respond(it.response())
            }

            get<Culebra.Help.Query> {
                call.respond(it.response())
            }

            get<Culebra.Quit> {
                println("Going to quit...")
                call.respondRedirect(shutdownUrl, permanent = true)
            }

            get<Configurator.GetWaitForIdleTimeout> {
                call.respond(it.response())
            }

            get<Configurator.SetWaitForIdleTimeout> {
                call.respond(it.response())
            }

            get<Device.DisplayRealSize> {
                call.respond(it.response())
            }

            get<Device.Dumpsys> {
                call.respond(it.response())
            }

            get<Device.Locale.Get> {
                call.respond(it.response())
            }

            post<Device.Locale.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val body = call.receive<Locale>()
                call.respond(it.response(body))
            }

            get<Device.WaitForNewToast> {
                call.respond(it.response())
            }

            get<ObjectStore.Clear> {
                call.respond(it.response())
            }

            get<ObjectStore.List> {
                call.respond(it.response())
            }

            get<ObjectStore.Remove> {
                call.respond(it.response())
            }

            get<TargetContext.StartActivity> {
                call.respond(it.response())
            }

            get<UiDevice.ClearLastTraversedText> {
                call.respond(it.response())
            }

            // 坐标点击的 HTTP 链路：
            // GET /v2/uiDevice/click?x=10&y=20 由 Ktor Locations 匹配并反序列化为
            // UiDevice.Click.Get。路由调用 response()，再通过 Holder.uiDevice 在指定屏幕坐标执行点击。
            get<UiDevice.Click.Get> {
                call.respond(it.response())
            }

            // 批量坐标点击的 HTTP 链路：
            // POST /v2/uiDevice/click 接收 JSON 格式的 ClickBody。Ktor 这里不会把请求体绑定到
            // Location 类，因此显式读取 body 后传给 UiDevice.Click.Post.response()，再逐个点击传入坐标。
            post<UiDevice.Click.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val body = call.receive<ClickBody>()
                call.respond(it.response(body))
            }

            get<UiDevice.DisplayHeight> {
                call.respond(it.response())
            }

            get<UiDevice.DisplayWidth> {
                call.respond(it.response())
            }

            get<UiDevice.Drag> {
                call.respond(it.response())
            }

            get<UiDevice.DumpWindowHierarchy> {
                // 后续可以根据 call.request.headers["accept"] 判断返回 XML 还是 JSON。
                call.respondText(it.response())
            }

            get<UiDevice.DisplayRotation> {
                call.respond(it.response())
            }

            get<UiDevice.DisplaySizeDp> {
                call.respond(it.response())
            }

            get<UiDevice.FindObject.Get> {
                call.respond(it.response())
            }

            post<UiDevice.FindObject.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val selector = call.receive<Selector>()
                call.respond(it.response(selector))
            }

            get<UiDevice.FindObjects.Get> {
                call.respond(it.response())
            }

            post<UiDevice.FindObjects.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val selector = call.receive<Selector>()
                call.respond(it.response(selector))
            }

            get<UiDevice.FreezeRotation> {
                call.respond(it.response())
            }

            get<UiDevice.CurrentPackageName> {
                call.respond(it.response())
            }

            get<UiDevice.IsNaturalOrientation> {
                call.respond(it.response())
            }

            get<UiDevice.IsScreenOn> {
                call.respond(it.response())
            }

            get<UiDevice.LastTraversedText> {
                call.respond(it.response())
            }

            get<UiDevice.PressBack> {
                call.respond(it.response())
            }

            get<UiDevice.PressDPadCenter> {
                call.respond(it.response())
            }

            get<UiDevice.PressDPadLeft> {
                call.respond(it.response())
            }

            get<UiDevice.PressDPadRight> {
                call.respond(it.response())
            }

            get<UiDevice.PressDPadUp> {
                call.respond(it.response())
            }

            get<UiDevice.PressDPadDown> {
                call.respond(it.response())
            }

            get<UiDevice.PressDelete> {
                call.respond(it.response())
            }

            get<UiDevice.PressEnter> {
                call.respond(it.response())
            }

            get<UiDevice.PressHome> {
                call.respond(it.response())
            }

            get<UiDevice.PressKeyCode> {
                call.respond(it.response())
            }

            get<UiDevice.PressRecentApps> {
                call.respond(it.response())
            }

            get<UiDevice.ProductName> {
                call.respond(it.response())
            }

            get<UiDevice.HasObject.Get> {
                call.respond(it.response())
            }

            post<UiDevice.HasObject.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val selector = call.receive<Selector>()
                call.respond(it.response(selector))
            }

            get<UiDevice.Pixel> {
                call.respond(it.response())
            }

            get<UiDevice.Screenshot> {
                call.respondImage(it.response())
            }

            get<UiDevice.Swipe.Get> {
                call.respond(it.response())
            }

            post<UiDevice.Swipe.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val body = call.receive<SwipeBody>()
                call.respond(it.response(body))
            }

            get<UiDevice.UnfreezeRotation> {
                call.respond(it.response())
            }

            get<UiDevice.Wait> {
                call.respond(it.response())
            }

            get<UiDevice.WaitForIdle> {
                call.respond(it.response())
            }

            get<UiDevice.WaitForWindowUpdate> {
                call.respond(it.response())
            }

            get<UiObject.ClearTextField> {
                call.respond(it.response())
            }

            // 旧版 UiObject 按钮点击的 HTTP 链路：
            // 调用方先通过 findObject 创建对象 id，再请求 GET /v2/uiObject/{oid}/click。
            // response() 会用 oid 从 Holder.objectStore 取回按钮对象，并对保存的 UiObject 执行 click()。
            get<UiObject.Click> {
                call.respond(it.response())
            }

            get<UiObject.ClickAndWaitForNewWindow> {
                call.respond(it.response())
            }

            get<UiObject.Dump> {
                call.respond(it.response())
            }

            get<UiObject.Exists> {
                call.respond(it.response())
            }

            get<UiObject.GetBounds> {
                call.respond(it.response())
            }

            get<UiObject.GetChild> {
                call.respond(it.response())
            }

            get<UiObject.GetChildCount> {
                call.respond(it.response())
            }

            get<UiObject.GetClassName> {
                call.respond(it.response())
            }

            get<UiObject.GetContentDescription> {
                call.respond(it.response())
            }

            get<UiObject.GetFromParent> {
                call.respond(it.response())
            }

            post<UiObject.PerformTwoPointerGesture.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val body = call.receive<PerformTwoPointerGestureBody>()
                call.respond(it.response(body))
            }

            get<UiObject.PinchIn> {
                call.respond(it.response())
            }

            get<UiObject.PinchOut> {
                call.respond(it.response())
            }

            get<UiObject.WaitForExists> {
                call.respond(it.response())
            }

            get<UiObject2.Clear> {
                call.respond(it.response())
            }

            // UiObject2 按钮点击的 HTTP 链路：
            // GET /v2/uiObject2/{oid}/click 会解析 findObject 或 findObjects 创建的对象 id，
            // 再对匹配到的按钮或视图调用 UiObject2.click()。
            get<UiObject2.Click> {
                call.respond(it.response())
            }

            get<UiObject2.ClickAndWait> {
                call.respond(it.response())
            }

            get<UiObject2.Dump> {
                call.respond(it.response())
            }

            get<UiObject2.FindObject.Get> {
                call.respond(it.response())
            }

            post<UiObject2.FindObject.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val selector = call.receive<Selector>()
                call.respond(it.response(selector))
            }

            get<UiObject2.GetChildCount> {
                call.respond(it.response())
            }

            get<UiObject2.GetChildren> {
                call.respond(it.response())
            }

            get<UiObject2.GetContentDescription> {
                call.respond(it.response())
            }

            get<UiObject2.GetText> {
                call.respond(it.response())
            }

            get<UiObject2.LongClick> {
                call.respond(it.response())
            }

            get<UiObject2.SetText.Get> {
                call.respond(it.response())
            }

            post<UiObject2.SetText.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val text = call.receive<Text>()
                call.respond(it.response(text))
            }

            get<Until.Dump> {
                call.respond(it.response())
            }

            get<Until.FindObject.Get> {
                call.respond(it.response())
            }

            post<Until.FindObject.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val selector = call.receive<Selector>()
                call.respond(it.response(selector))
            }

            get<Until.FindObjects.Get> {
                call.respond(it.response())
            }

            post<Until.FindObjects.Post> {
                // Ktor 不会自动把请求体放进 Location 对象，需要在路由里手动读取。
                // 参考：https://github.com/ktorio/ktor/issues/190
                // 这里的 it.body 为 null。
                // println("body ${it.body}");
                val selector = call.receive<Selector>()
                call.respond(it.response(selector))
            }

            get<Until.NewWindow> {
                call.respond(it.response())
            }

        }

        // 所有未匹配的路由都会走这里，并返回 404。
        route("{...}") {
            println("🦄")
            handle {
                println("🐹 not found:")
                println(call.request.local.uri)
                if (call.request.local.uri != shutdownUrl) {
                    call.respond(HttpStatusCode.NotFound, "${call.request.local.uri} not found")
                }
            }
        }

        install(StatusPages) {

            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }

            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

            exception<HttpException> { cause ->
                call.respond(cause.code, cause.description + "\n")
            }

            exception<Throwable> { cause ->
                val msg = "🛑 ERROR: $cause\n"
                System.err.print(msg)
                System.err.println(cause.message)
                cause.printStackTrace(System.err)
                call.respond(HttpStatusCode.InternalServerError, msg)
            }

            status(HttpStatusCode.NotFound) {
                call.respond(
                    TextContent(
                        "⛔️ ${it.value} ${it.description}",
                        ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                        it
                    )
                )
            }
        }

        AutoTesterServer().apply {
            println("++++ $this ++++")
        }
    }
}

/**
 * 按指定的 [contentType] 将 [file] 的内容作为响应返回。
 */
suspend inline fun ApplicationCall.respondImage(
    file: File,
    contentType: ContentType = ContentType.Image.PNG
) {
    // 这里不能直接设置响应头或使用 respondFile，否则会返回 500。
    // response.headers.append(HttpHeaders.ContentType, ContentType.Image.PNG.toString())
    //respondFile(file)
    val bytes = file.readBytes()
    respondBytes(bytes, contentType, HttpStatusCode.OK)
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
