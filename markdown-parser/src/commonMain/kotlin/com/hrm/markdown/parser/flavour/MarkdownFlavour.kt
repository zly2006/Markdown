package com.hrm.markdown.parser.flavour

import com.hrm.markdown.parser.block.postprocessors.PostProcessor
import com.hrm.markdown.parser.block.starters.BlockStarter

/**
 * Markdown 方言（Flavour）描述符。
 *
 * 定义了特定 Markdown 方言的解析规则和扩展特性。
 * 通过组合不同的块解析器、后处理器和配置选项，
 * 可以实现 CommonMark、GFM 或自定义 Markdown 语法。
 *
 * ## 使用示例
 *
 * ```kotlin
 * // 使用 CommonMark
 * val parser = MarkdownParser(CommonMarkFlavour)
 *
 * // 使用 GFM（GitHub Flavored Markdown）
 * val parser = MarkdownParser(GFMFlavour)
 *
 * // 自定义方言
 * val customFlavour = object : MarkdownFlavour {
 *     override val blockStarters = CommonMarkFlavour.blockStarters + listOf(
 *         CalloutBlockStarter(),
 *         WikiLinkStarter()
 *     )
 *     override val postProcessors = CommonMarkFlavour.postProcessors
 *     override val options = CommonMarkFlavour.options
 * }
 * ```
 */
interface MarkdownFlavour {
    /**
     * 块级解析器（BlockStarter）列表。
     *
     * 按优先级顺序排列，用于识别和解析块级元素（标题、列表、代码块等）。
     * 解析器会按顺序尝试每个 Starter，直到找到匹配的块类型。
     *
     * @see com.hrm.markdown.parser.block.starters.BlockStarter
     */
    val blockStarters: List<BlockStarter>

    /**
     * 后处理器（PostProcessor）列表。
     *
     * 在 AST 构建完成后执行，用于处理引用链接、脚注、标题 ID 生成等需要全局信息的任务。
     * 后处理器按优先级顺序执行。
     *
     * @see com.hrm.markdown.parser.block.postprocessors.PostProcessor
     */
    val postProcessors: List<PostProcessor>

    /**
     * 方言选项配置。
     *
     * 控制解析器的行为，如是否启用 GFM 自动链接、是否允许 HTML、表格对齐方式等。
     */
    val options: FlavourOptions
}

/**
 * Markdown 方言配置选项。
 *
 * @property enableGfmAutolinks 启用 GFM 自动链接（自动识别 URL 和邮箱）
 * @property enableGfmStrikethrough 启用 GFM 删除线 (`~~text~~`)
 * @property enableGfmTables 启用 GFM 表格
 * @property enableGfmTaskLists 启用 GFM 任务列表 (`- [ ] task`)
 * @property enableMath 启用数学公式 (`$...$` 和 `$$...$$`)
 * @property enableEmoji 启用 Emoji 短代码 (`:smile:`)
 * @property enableFootnotes 启用脚注 (`[^1]`)
 * @property enableDefinitionLists 启用定义列表
 * @property enableAbbreviations 启用缩写定义
 * @property enableSuperscript 启用上标 (`^text^`)
 * @property enableSubscript 启用下标 (`~text~`)
 * @property enableHighlight 启用高亮 (`==text==`)
 * @property enableInsertedText 启用插入文本标记 (`++text++`)
 * @property enableCustomContainers 启用自定义容器 (`::: type`)
 * @property enableFrontMatter 启用前置元数据 (`---` / `+++`)
 * @property allowHtmlBlocks 允许 HTML 块
 * @property allowInlineHtml 允许内联 HTML
 */
data class FlavourOptions(
    val enableGfmAutolinks: Boolean = false,
    val enableGfmStrikethrough: Boolean = false,
    val enableGfmTables: Boolean = false,
    val enableGfmTaskLists: Boolean = false,
    val enableMath: Boolean = false,
    val enableEmoji: Boolean = false,
    val enableFootnotes: Boolean = false,
    val enableDefinitionLists: Boolean = false,
    val enableAbbreviations: Boolean = false,
    val enableSuperscript: Boolean = false,
    val enableSubscript: Boolean = false,
    val enableHighlight: Boolean = false,
    val enableInsertedText: Boolean = false,
    val enableCustomContainers: Boolean = false,
    val enableFrontMatter: Boolean = false,
    val allowHtmlBlocks: Boolean = true,
    val allowInlineHtml: Boolean = true,
) {
    companion object {
        /**
         * 默认选项（CommonMark 标准）。
         */
        val Default = FlavourOptions()

        /**
         * GFM（GitHub Flavored Markdown）选项。
         */
        val GFM = FlavourOptions(
            enableGfmAutolinks = true,
            enableGfmStrikethrough = true,
            enableGfmTables = true,
            enableGfmTaskLists = true,
        )

        /**
         * 扩展选项（包含所有扩展特性）。
         */
        val Extended = FlavourOptions(
            enableGfmAutolinks = true,
            enableGfmStrikethrough = true,
            enableGfmTables = true,
            enableGfmTaskLists = true,
            enableMath = true,
            enableEmoji = true,
            enableFootnotes = true,
            enableDefinitionLists = true,
            enableAbbreviations = true,
            enableSuperscript = true,
            enableSubscript = true,
            enableHighlight = true,
            enableInsertedText = true,
            enableCustomContainers = true,
            enableFrontMatter = true,
        )
    }
}
