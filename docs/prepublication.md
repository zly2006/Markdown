# 正式发布前仍需完成的事项

源码已发布到 [zly2006/Markdown](https://github.com/zly2006/Markdown) 的 `main`，保留原仓库的 fork 关系。`0.1.0-alpha.1` 已于 2026-10-27 完成 Central 正式发布，所有平台构件已从公共仓库核对；后续版本继续按 `releasing.md` 执行。版本能力与迁移说明见 `releases/0.1.0-alpha.1.md`。

## 已整理的内容

Markdown 使用 `me.zly2006.markdown` 包名与 Maven group，parser、runtime、renderer 保持独立模块。上游 MIT 声明、分叉版权和 AndroidX Apache 2.0 声明分别保留，许可证随构件资源附带。LaTeX 与 CodeHigh 五个源码模块在同一构建中，避免依赖尚未确认可用的远端平台构件。

## 开源提交范围

内部验收工具不自动属于开源库的交付内容。为证明 Maven 坐标可消费而临时创建的应用，应放在仓库外，不得随库源码公开。发布前应检查实际跟踪文件及文档入口；用户要求从历史清除误提交内容时，还必须检查发布分支的可达历史，不能用后续删除提交代替清除。

## 发布决定

源码仓库已确定，POM 的项目 URL 和 SCM 指向该仓库。后续版本仍需确定 release 版本及维护方式；GitHub 源码公开与 Maven 构件发布应分别确认终态。

本次 Central 校验已确认当前账户能发布 `me.zly2006.markdown`。后续更换 namespace 或发布账户时，应重新核对权限；域名 namespace 的注册与所有权验证遵循 [Sonatype 的 namespace 说明](https://central.sonatype.org/faq/namespaces-and-dns/) 和 [Kotlin 的 Maven Central 发布教程](https://kotlinlang.org/docs/multiplatform/multiplatform-publish-libraries-to-maven.html)。

## 构件验证

至少需要检查 KMP 根 metadata、Android AAR、JVM JAR、所有计划发布的 Native KLIB、sources，以及 POM/Gradle Module Metadata 的依赖坐标。所有源码依赖必须一并发布，或改为已经验证具备全部平台构件的独立依赖；不能让 renderer 指向本机工程才能解析的模块。

本地预验证应使用工程的 `LocalVerification` 文件仓库，并检查坐标解析和公开类型的编译依赖。JVM 测试、Android 构建和 macOS 编译应达到终态。iOS 只有在对应目标实际构建并验收后，才可计入发布范围。

正式发布还需配置并验证源码包、API 文档、签名与 Central 规则要求的元数据。完整的 MIT 与 Apache 2.0 许可、原版权、修改说明和依赖归属应在最终构件中读回检查，依据 [MIT](https://opensource.org/license/mit) 与 [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) 的许可文本保留。

本轮已获得发布授权，以上事项必须完成并记录实际结果后，才能宣布发布与应用迁移完成。
