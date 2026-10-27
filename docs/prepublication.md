# 正式发布前仍需完成的事项

源码已获准发布到 [zly2006/Markdown](https://github.com/zly2006/Markdown) 的 `main`，保留原仓库的 fork 关系。Maven 构件仍只做本地验证：工程没有配置 Maven Central 凭据、签名、远端 publication repository 或发布工作流，构件任务只允许写入工程内的文件仓库。

## 已整理的内容

Markdown 使用 `me.zly2006.markdown` 包名与 Maven group，parser、runtime、renderer 保持独立模块。上游 MIT 声明、分叉版权和 AndroidX Apache 2.0 声明分别保留，许可证随构件资源附带。LaTeX 与 CodeHigh 五个源码模块在同一构建中，避免依赖尚未确认可用的远端平台构件。

## 开源提交范围

内部验收工具不自动属于开源库的交付内容。为证明 Maven 坐标可消费而临时创建的应用，应放在仓库外，不得随库源码公开。发布前应检查实际跟踪文件及文档入口；用户要求从历史清除误提交内容时，还必须检查发布分支的可达历史，不能用后续删除提交代替清除。

## 发布决定

源码仓库已确定，POM 的项目 URL 和 SCM 指向该仓库。正式 Maven 发布前仍需要确定 release 版本及维护方式；GitHub 源码公开不代表 Maven 构件已经发布。

还需要在 Maven Central 账户中确认 `me.zly2006` 或覆盖 `me.zly2006.markdown` 的 namespace 是否已经验证。包名本身不能证明发布权限；域名 namespace 的注册与所有权验证遵循 [Sonatype 的 namespace 说明](https://central.sonatype.org/faq/namespaces-and-dns/) 和 [Kotlin 的 Maven Central 发布教程](https://kotlinlang.org/docs/multiplatform/multiplatform-publish-libraries-to-maven.html)。本次没有查看账户发布权限，也没有创建 DNS 记录。

## 构件验证

至少需要检查 KMP 根 metadata、Android AAR、JVM JAR、所有计划发布的 Native KLIB、sources，以及 POM/Gradle Module Metadata 的依赖坐标。所有源码依赖必须一并发布，或改为已经验证具备全部平台构件的独立依赖；不能让 renderer 指向本机工程才能解析的模块。

本地预验证应使用工程的 `LocalVerification` 文件仓库，并检查坐标解析和公开类型的编译依赖。JVM 测试、Android 构建和 macOS 编译应达到终态。iOS 只有在对应目标实际构建并验收后，才可计入发布范围。

正式发布还需配置并验证源码包、API 文档、签名与 Central 规则要求的元数据。完整的 MIT 与 Apache 2.0 许可、原版权、修改说明和依赖归属应在最终构件中读回检查，依据 [MIT](https://opensource.org/license/mit) 与 [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) 的许可文本保留。

这些事项需要下一轮明确授权后再执行；当前不执行正式发布。
