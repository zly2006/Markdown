# Kotlin Multiplatform Markdown 渲染库

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-brightgreen.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android API](https://img.shields.io/badge/Android%20API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.huarangmeng/markdown-parser.svg)](https://central.sonatype.com/search?q=io.github.huarangmeng.markdown)

这是一个基于 Kotlin Multiplatform (KMP) 开发的高性能 Markdown 解析与渲染库。使用 Compose Multiplatform 在 Android、iOS、Desktop (JVM) 和 Web (Wasm/JS) 平台上实现一致的渲染效果。

[English Version](./README.md)

## 🌟 核心特性

- **高性能解析**：基于 AST 的递归下降解析器，支持增量更新。
- **多平台一致性**：使用 Compose Multiplatform 在 Android、iOS、Desktop (JVM) 和 Web (Wasm/JS) 平台上实现一致渲染。
- **全面的语法覆盖**：已支持 229/243 项 Markdown 特性（94% 覆盖率），涵盖 CommonMark、GFM 及常见扩展语法。
- **内置图片加载**：集成 Coil3 + Ktor3，开箱即用的网络图片加载，支持指定尺寸、自适应宽度，也支持自定义图片渲染器。
- **流式渲染**：一等公民级别的 LLM 逐 token 输出支持。增量解析 + 节流渲染（5fps），流式生成期间消除闪烁。
- **可定制主题**：完整的主题系统，30+ 可配置属性。内置亮色/暗色主题（GitHub 风格），自动跟随系统日夜间模式。
- **LaTeX 数学公式**：支持行内 (`$...$`) 和块级 (`$$...$$`) 数学公式，集成 LaTeX 渲染引擎。
- **增量解析**：感知编辑操作的解析器，仅重新解析受影响区域，适用于实时编辑场景。
- **分页加载**：超长文档（500+ 块）渐进式渲染，滚动到底部自动加载更多内容。

## 📐 已支持的 Markdown 功能（229+）

<details>
<summary><b>块级元素</b> — 标题、段落、代码块、列表、表格等</summary>

- **标题**：ATX 标题 (`# ~ ######`)、Setext 标题 (`===` / `---`)、自定义标题 ID (`{#id}`)
- **段落**：多行合并、空行分隔、延续行
- **代码块**：围栏代码块 (`` ``` `` / `~~~`) 支持语言标识、缩进代码块（4 空格/Tab）
- **块引用**：嵌套引用、延续行、内部块级元素
- **列表**：无序列表 (`-`, `*`, `+`)、有序列表 (`1.`, `1)`)、任务列表 (`- [ ]` / `- [x]`)、嵌套列表、紧凑/松散区分
- **表格 (GFM)**：列对齐 (`:---`, `:---:`, `---:`)、单元格内行内元素、管道符转义
- **分隔线**：`---`、`***`、`___`
- **HTML 块**：所有 7 种 CommonMark 类型
- **链接引用定义**：完整支持各种标题格式
</details>

<details>
<summary><b>行内元素</b> — 强调、链接、图片、代码等</summary>

- **强调**：粗体 (`**`/`__`)、斜体 (`*`/`_`)、粗斜体 (`***`/`___`)、嵌套强调
- **删除线 (GFM)**：`~~text~~`、`~text~`
- **行内代码**：单/多反引号、空格剥离、内部不解析
- **链接**：行内链接、引用链接（完整/折叠/简写）、自动链接（URL/邮箱/GFM 裸 URL）
- **图片**：行内图片、引用图片、嵌套在链接内
- **行内 HTML**：标签、注释、CDATA、处理指令
- **转义与实体**：反斜杠转义、命名/数字 HTML 实体
- **硬/软换行**：行尾空格、反斜杠
</details>

<details>
<summary><b>扩展语法</b> — 数学公式、脚注、告示块等</summary>

- **数学公式**：行内 `$...$`、块级 `$$...$$`
- **脚注**：`[^label]` 引用、`[^label]: content` 定义、多行脚注、脚注内块级元素
- **告示/提醒块**：`> [!NOTE]`、`> [!TIP]`、`> [!IMPORTANT]`、`> [!WARNING]`、`> [!CAUTION]`
- **高亮**：`==text==`
- **上标 / 下标**：`^text^`、`~text~`、`<sup>`、`<sub>`
- **插入文本**：`++text++`
- **Emoji**：`:emoji_name:` 短代码、Unicode Emoji
- **定义列表**：术语 + `: definition` 格式
- **Front Matter**：YAML (`---`) 和 TOML (`+++`)
</details>

## 🛠️ 使用方法

在 Compose Multiplatform 项目中，直接使用 `Markdown` 组件：

```kotlin
import com.hrm.markdown.renderer.Markdown
import com.hrm.markdown.renderer.MarkdownTheme

@Composable
fun MyScreen() {
    Markdown(
        markdown = """
            # Hello World
            
            这是一个包含 **粗体** 和 *斜体* 的段落。
            
            - 列表项 1
            - 列表项 2
            
            ```kotlin
            fun hello() = println("Hello")
            ```
        """.trimIndent(),
        modifier = Modifier.fillMaxSize(),
        theme = MarkdownTheme.auto(), // 自动跟随系统日夜间模式
    )
}
```

### 流式渲染（LLM 集成）

在 LLM 逐 token 输出场景下，使用 `isStreaming` 参数启用增量解析和节流渲染：

```kotlin
var text by remember { mutableStateOf("") }
var isStreaming by remember { mutableStateOf(true) }

LaunchedEffect(Unit) {
    tokens.collect { token ->
        text += token
    }
    isStreaming = false
}

Markdown(
    markdown = text,
    isStreaming = isStreaming,
)
```

### 自定义主题

```kotlin
// 使用内置主题
Markdown(markdown = text, theme = MarkdownTheme.light())  // GitHub 亮色
Markdown(markdown = text, theme = MarkdownTheme.dark())   // GitHub 暗色
Markdown(markdown = text, theme = MarkdownTheme.auto())   // 跟随系统

// 或完全自定义
Markdown(
    markdown = markdownText,
    theme = MarkdownTheme(
        headingStyles = listOf(
            TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            // h2 ~ h6 ...
        ),
        bodyStyle = TextStyle(fontSize = 16.sp),
        codeBlockBackground = Color(0xFFF5F5F5),
        // 30+ 可配置属性...
    ),
    onLinkClick = { url -> /* 处理链接点击 */ }
)
```

### 图片加载

库内置了基于 Coil3 的图片加载能力，Markdown 中的图片语法会自动从网络加载并渲染：

```markdown
![描述文本](https://example.com/image.png)
![指定尺寸](https://example.com/image.png =200x100)
```

如需自定义图片渲染逻辑（例如添加 loading 占位符、错误状态等），可通过 `imageContent` 参数覆盖：

```kotlin
Markdown(
    markdown = markdownText,
    imageContent = { data, modifier ->
        // data.url, data.altText, data.width, data.height 等信息均可获取
        AsyncImage(
            model = data.url,
            contentDescription = data.altText,
            modifier = modifier,
        )
    },
)
```

## 📦 安装

在 `gradle/libs.versions.toml` 中添加依赖：

```toml
[versions]
markdown = "1.0.3"

[libraries]
markdown-parser = { module = "io.github.huarangmeng:markdown-parser", version.ref = "markdown" }
markdown-renderer = { module = "io.github.huarangmeng:markdown-renderer", version.ref = "markdown" }
```

在模块的 `build.gradle.kts` 中引用：

```kotlin
dependencies {
    implementation(libs.markdown.parser)
    implementation(libs.markdown.renderer)
}
```

> `markdown-renderer` 内置了 Coil3 + Ktor3 用于图片加载，会作为传递依赖自动引入。

## 🏗️ 项目结构

- `:markdown-parser` — 核心解析引擎，负责将 Markdown 字符串转换为 AST（抽象语法树）。
- `:markdown-renderer` — 渲染引擎，负责将 AST 节点映射为 Compose UI 组件。
- `:markdown-preview` — 预览/演示模块，提供分类浏览的交互式 UI，展示所有支持的 Markdown 功能的渲染效果。
- `:composeApp` — 跨平台 Demo 应用程序（Android/iOS/Desktop/Web）。
- `:androidApp` — Android Demo 应用程序。

## 🚀 快速开始

### 运行 Demo App

- **Android**: `./gradlew :composeApp:assembleDebug`
- **Desktop**: `./gradlew :composeApp:run`
- **Web (Wasm)**: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- **Web (JS)**: `./gradlew :composeApp:jsBrowserDevelopmentRun`
- **iOS**: 在 Xcode 中打开 `iosApp` 目录运行。

### 运行测试

```bash
# Parser 模块测试
./gradlew :markdown-parser:jvmTest

# Renderer 模块测试
./gradlew :markdown-renderer:jvmTest

# 全部测试
./gradlew jvmTest
```

## 📊 路线图与功能覆盖

详细的功能支持列表请参阅：[PARSER_COVERAGE_ANALYSIS.md](./markdown-parser/PARSER_COVERAGE_ANALYSIS.md)

| 类别 | 覆盖率 |
|------|--------|
| 标题 | 88% |
| 段落与空行 | 100% |
| 代码块 | 93% |
| 块引用 | 100% |
| 列表 | 100% |
| 表格 (GFM) | 83% |
| 强调 | 100% |
| 链接 | 95% |
| 行内扩展 | 86% |
| 增量解析 | 100% |
| **总计** | **94%** |

## 📄 开源协议

本项目采用 MIT License 开源协议 - 详见 [LICENSE](LICENSE) 文件。

```
MIT License

Copyright (c) 2026 huarangmeng

特此免费授予任何获得本软件及相关文档文件（"软件"）副本的人不受限制地处理
软件的权利，包括但不限于使用、复制、修改、合并、发布、分发、再许可和/或
销售软件副本的权利，以及允许获得软件的人这样做，但须符合以下条件：

上述版权声明和本许可声明应包含在软件的所有副本或主要部分中。

本软件按"原样"提供，不提供任何形式的明示或暗示保证，包括但不限于对适销性、
特定用途的适用性和非侵权性的保证。在任何情况下，作者或版权持有人均不对
因软件或软件的使用或其他交易而产生的任何索赔、损害或其他责任承担责任，
无论是在合同诉讼、侵权行为还是其他方面。
```
