# 独立工程验证记录

本轮验证日期为 2026-10-16。来源固定为 Zhihu++ 提交 `71bf1d8a19e1f413b5ad8418503317a64253294f`，没有切换为上游最新版，也没有修改来源工程。

## 来源与代码整理

`docs/source-snapshot.json` 共记录 333 个导入文件。按指定 Git 提交读回原始文件，并对整理后的目标文件逐项计算 SHA-256：来源与目标均为 0 项失配。五个支持模块的原源码和构建适配文件保留原包名及原版权。

三个 Markdown 模块的包名、import、源码路径和 Android namespace 已迁至 `me.zly2006.markdown`；143 个 Kotlin 源码或 Gradle Kotlin 脚本均保留上游版权并加入 Liyan Zhao（zly2006）的声明。10 个 AndroidX 文件保留 Apache 2.0 标头和修改说明。另完成了显式 import、格式化、内部文件名和私有局部命名的整理；没有替换渲染实现。

## 构建与测试

| 验证面 | 实际执行 | 结果 |
| --- | --- | --- |
| JVM | parser 与 renderer 的 `jvmTest` | Gradle 测试报告共 33 项，失败、错误均为 0 |
| 超宽行内项 | 新同步的 `InlineFlowOverflowTest` | 900sp 行内项在 300px 可用宽度下夹窄布局，回归用例通过 |
| 列表与表格布局 | renderer 的 6 项布局回归 | 全部通过，包含图片开头、跨编号位数、换行和混排表格 |
| Android | 八个模块的 Android publication/AAR 任务 | 全部完成，renderer AAR 生成并读回检查 |
| macOS ARM64 | `:markdown-renderer:compileKotlinMacosArm64` 及其依赖 | 编译完成，KLIB 输出存在 |
| 格式 | 先单独执行 `ktlintFormat`，再单独执行 `ktlintCheck` | 两次调用均完成；检查通过 |
| iOS | 本轮未执行 iOS 平台编译或设备验收 | 不计入已验证发布范围 |

测试报告中的性能实验受 `MARKDOWN_PERFORMANCE=1` 控制，本轮没有启用六项性能实验；33 项报告的成功不代表完成了新的性能基准。macOS 验证是库的编译验证，没有启动原生窗口或声明物理输入链已经验收。

## 本地 Maven 构件

`stageLocalArtifacts` 已将八个模块的 KMP 根、JVM 和 Android publication 共 24 组构件写入 `build/local-maven/`。按每个坐标最新的 Maven metadata 读回，而不是挑选目录中较旧的 snapshot 文件。

24 组构件的 group 均为 `me.zly2006.markdown`、version 均为 `0.1.0-SNAPSHOT`，二进制和 sources 存在。根/JVM JAR 及 Android AAR 内的 `classes.jar` 均包含 MIT、上游 MIT、Apache 2.0 和 NOTICE。Markdown JVM 构件没有旧 `com.hrm.markdown` 类路径，sources 与当前源码一致；生产构件没有公式测试数据。详细读回结果位于本轮本地验证输出 `build/verification/artifacts.json`，错误数为 0。

## 发布边界与剩余事项

源码仓库确定为 `https://github.com/zly2006/Markdown`，origin 已配置；当前没有 Maven 签名凭据、远端 Maven 构件仓库或发布工作流。`PublishToMavenLocal` 已禁用，本地 publication 执行前检查目录必须与工程内的验证仓库一致。由于 Gradle 9.7.1 的配置缓存会丢弃发布任务的 repository 模型，只有执行目录检查的 publication 任务禁用该缓存；普通构建和测试仍使用配置缓存。

正式 Maven 版本号、Central namespace 权限、签名、API 文档和所有拟发布 Native 构件仍按 `prepublication.md` 留待下一阶段。当前没有向 Maven Central 或其他外部构件仓库上传构件。GitHub 源码推送与 Maven 正式发布是两项独立操作；本轮仅执行前者。
