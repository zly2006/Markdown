# Maven Central 发布

当前发布坐标为 `me.zly2006.markdown`，首个预览版为 `0.1.0-alpha.1`。版本由根目录 `gradle.properties` 的 `libraryVersion` 统一管理。八个模块及其 KMP 根、Android、JVM、iOS ARM64、iOS Simulator ARM64 和 macOS ARM64 构件必须作为同一组发布，不能让 renderer 引用未公开的支持模块。

## 构建与校验

先执行 `./gradlew stageReleaseArtifacts :markdown-parser:jvmTest :markdown-renderer:jvmTest`。该任务只把所有平台的构件、sources、Dokka 文档和 GPG 签名写入工程内 `build/local-maven/`，不会上传远端。确认所有预期构件和签名存在，再分别执行格式化与格式检查。

## 上传与正式发布

`./gradlew publishToMavenCentral` 上传经过签名的构件，默认不自动正式发布。Central 返回 `VALIDATED` 后，可通过 Portal 或官方 Publisher API 执行发布，并持续确认 `PUBLISHED`。失败时检查已有 deployment，避免为同一版本重复上传或覆盖已发布构件。

发布完成后，从 Maven Central 公共下载地址读回 POM、Gradle metadata、平台二进制、sources 和签名，并确认下游项目只使用公共仓库即可解析。只有这一步完成，应用才能删除相应内置源码并切换到新坐标。

## 凭据与归属

使用发布者本机已经配置的 Central user token 和 GPG 签名设置。凭据、私钥及会话不得写进构建脚本、文档或 Git。临时发布工具应在仓库外运行；如需保存其凭据，应与工具同目录、使用有意义的点文件名、限制权限并加入忽略规则。

保留上游 MIT、AndroidX Apache 2.0 和分叉版权说明。发布流程不自动升级源代码基线，也不合入未验证的上游新版本。
