# autotest



## 文档

当前项目android自动化测试。通过http进行请求，主要通过UiAutomation

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
