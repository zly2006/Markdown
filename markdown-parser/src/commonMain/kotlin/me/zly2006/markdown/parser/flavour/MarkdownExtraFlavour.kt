/*
 * Copyright (c) 2026 huarangmeng
 * Copyright (c) 2026 Liyan Zhao (zly2006)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zly2006.markdown.parser.flavour

import me.zly2006.markdown.parser.block.postprocessors.AbbreviationProcessor
import me.zly2006.markdown.parser.block.postprocessors.BlockAttributeProcessor
import me.zly2006.markdown.parser.block.postprocessors.HeadingIdProcessor
import me.zly2006.markdown.parser.block.postprocessors.PostProcessor
import me.zly2006.markdown.parser.block.starters.BlockQuoteStarter
import me.zly2006.markdown.parser.block.starters.BlockStarter
import me.zly2006.markdown.parser.block.starters.DefinitionDescriptionStarter
import me.zly2006.markdown.parser.block.starters.FencedCodeBlockStarter
import me.zly2006.markdown.parser.block.starters.FootnoteDefinitionStarter
import me.zly2006.markdown.parser.block.starters.HeadingStarter
import me.zly2006.markdown.parser.block.starters.HtmlBlockStarter
import me.zly2006.markdown.parser.block.starters.IndentedCodeBlockStarter
import me.zly2006.markdown.parser.block.starters.ListItemStarter
import me.zly2006.markdown.parser.block.starters.SetextHeadingStarter
import me.zly2006.markdown.parser.block.starters.TableStarter
import me.zly2006.markdown.parser.block.starters.ThematicBreakStarter

/**
 * PHP Markdown Extra compatible flavour.
 *
 * based on the [PHP Markdown Extra](https://michelf.ca/projects/php-markdown/extra/)
 * specification. extends CommonMark with:
 * - definition lists (`Term\n: Definition`)
 * - abbreviations (`*[HTML]: HyperText Markup Language`)
 * - footnotes (`[^1]: footnote content`)
 * - fenced code blocks (already in CommonMark)
 * - tables
 *
 * intentionally does NOT include:
 * - custom containers (`::: type ... :::`)
 * - math blocks (`$$ ... $$`) or inline math (`$...$`)
 * - emoji shortcodes (`:smile:`)
 * - directives (`{% tag %}`)
 * - front matter
 * - page breaks
 * - admonitions
 * - highlight, superscript, subscript, inserted text
 *
 * ## usage
 *
 * ```kotlin
 * val parser = MarkdownParser(MarkdownExtraFlavour)
 * val doc = parser.parse("""
 *     Term
 *     : Definition
 *
 *     *[HTML]: HyperText Markup Language
 *
 *     The HTML specification is maintained by the W3C.
 *
 *     [^1]: This is a footnote.
 *
 *     | Name | Age |
 *     | ---- | --- |
 *     | Bob  | 30  |
 * """.trimIndent())
 * ```
 */
object MarkdownExtraFlavour : MarkdownFlavour {
    /**
     * block starters for PHP Markdown Extra.
     *
     * includes CommonMark core starters plus:
     * - tables (200)
     * - footnote definitions (510)
     * - definition descriptions (520)
     *
     * does not include: FrontMatter, CustomContainer, MathBlock,
     * PageBreak, DirectiveBlock.
     */
    override val blockStarters: List<BlockStarter> = listOf(
        SetextHeadingStarter(), // 100
        HeadingStarter(), // 110
        TableStarter(), // 200
        ThematicBreakStarter(), // 210
        FencedCodeBlockStarter(), // 310
        HtmlBlockStarter(), // 400
        BlockQuoteStarter(), // 410
        ListItemStarter(), // 500
        FootnoteDefinitionStarter(), // 510
        DefinitionDescriptionStarter(), // 520
        IndentedCodeBlockStarter(), // 600
    )

    /**
     * post processors for PHP Markdown Extra.
     *
     * includes:
     * - heading id generation (100)
     * - block attribute parsing (150)
     * - abbreviation replacement (200)
     *
     * does not include: DiagramProcessor, ColumnsLayoutProcessor.
     */
    override val postProcessors: List<PostProcessor> = listOf(
        HeadingIdProcessor(), // 100
        BlockAttributeProcessor(), // 150
        AbbreviationProcessor(), // 200
    )

    /** gfm autolinks are not part of PHP Markdown Extra */
    override val enableGfmAutolinks: Boolean = false

    /** extended inline syntax (math, emoji, highlight, etc.) is not part of PHP Markdown Extra */
    override val enableExtendedInline: Boolean = false
}
