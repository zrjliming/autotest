# autotest

当前项目已迁移 `/Users/ruijie/daypop/code/demo/CulebraTester2-public` 的 Ktor/UiAutomator 服务能力，包名保持为 `com.cst.autotest`。

## 文档

- [迁移说明](docs/culebra-ktor-migration.md)

## 构建验证

```sh
./gradlew :app:assembleDebug
./gradlew :app:assembleDebugAndroidTest
```

## 启动 Ktor 服务

服务由 instrumentation helper 初始化 `UiDevice`、`UiAutomation`、`WindowManager` 等对象后启动，默认端口为 `9987`。

```sh
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.cst.autotest.UiAutomatorHelper
```

启动后可通过 adb 端口转发访问：

```sh
adb forward tcp:9987 tcp:9987
curl http://localhost:9987/
curl http://localhost:9987/help
curl http://localhost:9987/v2/culebra/info
```
