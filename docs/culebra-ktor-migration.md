# CulebraTester2 Ktor Migration

## 概述

本项目参考 Ktor HTTP API 和 Android UiAutomator 自动化能力

当前项目包名保持为 `com.cst.autotest`，Ktor 路由、Location 类、Swagger model、Selector 工具和 instrumentation helper 

## 主要模块

- `com.cst.autotest.KtorApplicationKt.module`
  - Ktor 服务入口。
  - 注册 `/`、`/help`、`/quit`、`/v2/*` 路由。
- `com.cst.autotest.UiAutomatorHelper`
  - Android instrumentation 启动器。
  - 初始化 `Holder` 中的 `UiDevice`、`UiAutomation`、`WindowManager`、`cacheDir`。
  - 启动 Ktor `EngineMain`，默认监听端口 `9987`。
- `com.cst.autotest.Holder`
  - 保存 instrumentation 环境对象。
  - 提供共享 `ObjectStore`。
- `com.cst.autotest.location.*`
  - 对应原项目的 `/v2` API 业务逻辑。
  - 包含 `UiDevice`、`UiObject`、`UiObject2`、`Until`、`Device`、`Configurator`、`ObjectStore` 等路由类。
- `com.cst.autotest.utils.*`
  - Selector、Locale、Package 工具。
- `io.swagger.server.models.*`
  - API 请求和响应模型，保留原 JSON 字段兼容性。

## 与参考项目的差异

- 当前项目保留 `com.cst.autotest` applicationId 和 namespace。
- 保留 Ktor/Kotlin 实现，不强制纯 Java。
- 未继续使用 Dagger/Hilt 生成注入；迁移后通过 `Holder` 和共享 `ObjectStore` 管理运行时对象。
- `MainActivity` 使用简单 Java 状态页，不迁移参考项目 Compose UI。
- 依赖和 Gradle 配置按当前 AGP 9 项目调整。

## 构建

```sh
./gradlew :app:assembleDebug
./gradlew :app:assembleDebugAndroidTest
```

以上两个命令已经验证通过。

## 安装与启动

安装 app 和 androidTest APK：

```sh
./gradlew installDebug installDebugAndroidTest
```

启动 Ktor 服务：

```sh
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.cst.autotest.UiAutomatorHelper
```

端口转发：

```sh
adb forward tcp:9987 tcp:9987
```

基础验证：

```sh
curl http://localhost:9987/
curl http://localhost:9987/help
curl http://localhost:9987/v2/culebra/info
curl http://localhost:9987/v2/uiDevice/currentPackageName
```

截图接口：

```sh
curl http://localhost:9987/v2/uiDevice/screenshot --output screenshot.png
```

## 权限与运行条件

- 服务必须通过 instrumentation helper 启动，普通 app 进程无法独立获得完整 UiAutomator 能力。
- `DUMP`、`PACKAGE_USAGE_STATS`、`CHANGE_CONFIGURATION` 属于受保护权限，部分接口仍依赖设备环境和测试权限。
- API 行为与参考项目一致：客户端应先启动 instrumentation server，再访问 `/v2/*` 路由。

## 已验证

- `./gradlew :app:assembleDebug`
- `./gradlew :app:assembleDebugAndroidTest`

## 后续建议

- 增加 Ktor 路由单元测试，覆盖 `/`、`/help`、`/v2/culebra/info`。
- 增加 instrumentation smoke test，验证 `currentPackageName`、`dumpWindowHierarchy`、`screenshot`。
- 根据实际调用端补充常用 API 示例。
