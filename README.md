# Markdown

这是从 [Zhihu++](https://github.com/zly2006/zhihu-plus-plus) 内置源码整理出的独立 Kotlin Multiplatform Markdown 工程，源码仓库为 [zly2006/Markdown](https://github.com/zly2006/Markdown)，保留在 [huarangmeng/Markdown](https://github.com/huarangmeng/Markdown) 的 fork 网络中。

当前同步包含列表缩进、marker 对齐、表格边框，以及超宽行内公式的布局和横向滚动修复。准确的来源提交、文件与哈希见 `docs/source-snapshot.json`。

Markdown 包名前缀和 Maven group 为 `me.zly2006.markdown`。首个预览版 `0.1.0-alpha.1` 已发布到 Maven Central，版本说明见 `docs/releases/0.1.0-alpha.1.md`。

```kotlin
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("me.zly2006.markdown:markdown-renderer:0.1.0-alpha.1")
}
```

KMP 项目将依赖放在 `commonMain.dependencies` 中，Gradle 会选择相应平台构件。renderer 会传递 parser、runtime、LaTeX 和 CodeHigh，不需要逐项添加。当前已发布 Android、JVM、iOS ARM64、iOS Simulator ARM64 和 macOS ARM64 构件。

## 模块与使用入口

| 模块 | 作用 | Kotlin 包名前缀 |
| --- | --- | --- |
| `markdown-parser` | 文档 AST、Markdown 与 HTML 解析 | `me.zly2006.markdown.parser` |
| `markdown-runtime` | 输入转换与 directive 扩展 | `me.zly2006.markdown.runtime` |
| `markdown-renderer` | Compose 布局、公式、代码高亮与选择 | `me.zly2006.markdown.renderer` |

LaTeX 与 CodeHigh 的五个模块放在 `third_party/` 中，使用同一工程构建，保留原包名。renderer 的公共 Compose、数学字体和高亮类型已通过 API 依赖声明传递给使用方。

```kotlin
import me.zly2006.markdown.renderer.Markdown
import me.zly2006.markdown.renderer.MarkdownTheme

Markdown(
    markdown = "# 示例\n\n- 第一项\n- 第二项",
    theme = MarkdownTheme.light(),
)
```

## 本地构建

需要 JDK 21 和 Android SDK 37。将 SDK 路径写入不入库的 `local.properties`，再运行：

```bash
./gradlew :markdown-parser:jvmTest :markdown-renderer:jvmTest
./gradlew :markdown-renderer:bundleAndroidMainAar
./gradlew :markdown-renderer:compileKotlinMacosArm64
./gradlew ktlintFormat
```

实际执行的目标与结果记录在 `docs/verification.md`。不要用总 `build` 任务的成功推断未执行的 iOS 或其他平台已经通过。

## 本地 Maven 构件

工程已配置 Gradle `maven-publish`，仓库只指向工程内的 `build/local-maven/`。例如 JVM 构件可通过下列任务验证：

```bash
./gradlew stageLocalArtifacts
```

这会把八个模块的 KMP 根、JVM 和 Android 构件写入本地目录；不会上传 Maven Central，也不会写入用户的 `~/.m2`。完整平台的签名构件可用 `stageReleaseArtifacts` 在本地构建，正式发布流程见 `docs/releasing.md`。

## Android 许可证资源

八个模块都附带完整许可文本。如果 Android 应用报告同名许可资源冲突，应合并这些文件，保留其内容：

```kotlin
android {
    packaging.resources.merges += listOf(
        "META-INF/LICENSE-MIT.txt",
        "META-INF/LICENSE-UPSTREAM-MIT.txt",
        "META-INF/LICENSE-APACHE-2.0.txt",
        "META-INF/NOTICE.md",
    )
}
```

来源、API 迁移和依赖边界见 `docs/architecture.md`；原始清单见 `docs/source-snapshot.json`；版权归属见 `NOTICE.md`。
