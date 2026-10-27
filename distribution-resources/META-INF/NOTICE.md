# 源码来源与版权

Markdown parser、runtime 与 Compose renderer 来自 huarangmeng 的 Markdown 项目，以及 Liyan Zhao（zly2006）在 Zhihu++ 中维护的分叉。MIT 原版权声明和许可文本保留在 `LICENSE` 与 `licenses/MIT-huarangmeng.txt` 中。

Markdown 模块增加 `Copyright (c) 2026 Liyan Zhao (zly2006)`。这一声明对应分叉修改、包名迁移和独立工程整理，不替代原作者声明。

renderer 中带有 Android Open Source Project 声明的文字选择实现继续采用 Apache License 2.0。这些文件保留原始年份、作者和许可标头，同时标明包名迁移等修改。完整许可文本位于 `licenses/Apache-2.0.txt`，文件清单见 `docs/source-snapshot.json`。

`third_party/latex` 与 `third_party/codehigh` 是当前 renderer 的源码依赖，保留各自的 MIT 许可证、原版权和原包名。它们的来源提交记录在各自 README 中，本次没有升级到远端新版本。

Gradle Wrapper 保留 Gradle 原声明。测试中的公式 JSON 来自原工程的测试资源，仅用于测试，不属于生产源码或 Maven 生产构件。

许可文件还通过 `distribution-resources/META-INF` 随平台资源打包；正式发布前应按 `docs/prepublication.md` 再核对所有目标构件。
