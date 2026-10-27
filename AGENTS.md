# Markdown 维护约束

- 源码来源和模块边界以 `docs/architecture.md`、`docs/source-snapshot.json` 为准。不得用上游最新版替换本次从 Zhihu++ 提取的快照。
- Markdown 的 Kotlin package 与 Maven group 使用 `me.zly2006.markdown`；内置 LaTeX 和 CodeHigh 保留原包名及原许可证。
- 保留所有原作者和 AndroidX 的声明。修改 Apache 2.0 文件时保留许可证并更新修改说明；完整归属见 `NOTICE.md`。
- 先构建和测试，再格式化，再做格式检查；格式化与检查必须分为两次串行 Gradle 调用。迁移应通过 JVM 测试、Android AAR 和已声明的 macOS 编译；验证方法见 `docs/prepublication.md`。
- GitHub 源码允许推送到 `main`，Maven 仍只允许项目内的本地验证仓库。不得新增远端 Maven 发布、签名凭据、发布工作流或正式 release；待确认项目见 `docs/prepublication.md`。
- 不得修改来源 Zhihu++ 工程，不得清理用户全局 Gradle、Kotlin/Native 或 Android SDK 缓存。
- 不得把内部验收脚手架当作开源交付内容；发布前检查跟踪文件与文档引用，历史清除要求按 `docs/prepublication.md` 的“开源提交范围”执行。
