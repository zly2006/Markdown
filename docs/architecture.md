# 来源、模块边界与包名迁移

本工程同步自 [Zhihu++ 提交 `71bf1d8a19e1f413b5ad8418503317a64253294f`](https://github.com/zly2006/zhihu-plus-plus/tree/71bf1d8a19e1f413b5ad8418503317a64253294f/third_party/markdown)，包含列表、表格及超宽行内公式修复。源码唯一基准是该提交中的内置库，没有整体替换为 huarangmeng 的最新分支。

上一轮来源提交为 `b01dcc8df6daf7842a56c4f7bc7bf1374de6a829`。本轮更新了三个行内渲染文件，并同步一项公式溢出回归测试；parser、runtime、LaTeX 和 CodeHigh 的来源内容没有新增变化。

Markdown 的上游基础版本为 1.2.9，提交 `0ae14148bbe427e27629117b3581ea071d86c4c7`；Zhihu++ 的后续变更已经包含在本次提取的快照中。新命名空间下的 `0.1.0-SNAPSHOT` 是独立工程的本地版本标识，不代表上游版本升级，也不是已经发布的版本。

## 模块依赖

`markdown-runtime` 依赖 parser。renderer 依赖 parser 和 runtime，并使用 LaTeX 的 base、parser、renderer，以及 CodeHigh 的 parser、render。为了让 Android、JVM、iOS 与 macOS 能独立解析完整依赖，当前将这五个支持模块作为同一工程的源码依赖保留。

LaTeX 的来源是 `zly2006/latex@fd40597f9c4b197523aac97be8b5009acfb70638`；CodeHigh 的来源是 `huarangmeng/CodeHigh@c18c1fbcd1e60131c1782e079801413307a41464`。二者本次只接入独立工程的构建和本地构件验证，源码包名仍为 `com.hrm.latex` 与 `com.hrm.codehigh`。

当前八个模块共享的 Maven group 用于本地验证。LaTeX 与 CodeHigh 的正式发布坐标还需要下一轮确定；使用方应避免同时引入另一套包含相同 `com.hrm.*` 类型的构件。后续可以继续共同发布这五个模块，也可以改用已经验证完整平台覆盖的独立依赖。

## API 迁移

Markdown 的 `com.hrm.markdown.*` 整体迁至 `me.zly2006.markdown.*`，包括 Kotlin 源码目录、import、Android namespace、测试与相关配置。使用者必须更新 import；这属于包名层面的兼容性变更，因此后续不能把新构件直接当作旧 Maven 坐标的原位替换。

renderer 的公开类型涉及 Compose 的 Modifier、TextStyle、ScrollState、ColorScheme，以及数学字体和高亮主题。这些依赖使用 API 方式声明，支持模块沿用各自已有的 API 依赖传递关系。

当前保留 Android、JVM、iOS ARM64、iOS Simulator ARM64 和 macOS ARM64 的既有目标。是否补充其他目标应另行决定，不能将未编译的目标报告为可发布。

文字选择桥接沿用当前 Compose 1.11.1 的内部 API，并保留 macOS 编译所需的 friend-module 配置。此轮只验证了当前固定依赖组合；后续升级 Compose 或声明更宽的兼容范围时，必须重新验证选择、滚动和平台链接。

## 独立性

独立工程包含 Gradle Wrapper、插件版本、仓库、平台构建配置、consumer rules、许可证与测试资源。两个性能测试读取的公式数据已经改为本工程 classpath 资源，不再读取 Zhihu++ 的 sibling 目录。项目不依赖 Zhihu++ 的 buildSrc、账户模型、app、shared 或其他业务模块。

`docs/source-snapshot.json` 记录原始文件哈希、导入位置与允许的整理变更。后续维护应分别审查源代码行为变化、版权声明变化和 Gradle 构件配置变化。
