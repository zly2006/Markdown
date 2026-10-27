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

package me.zly2006.markdown.renderer

import me.zly2006.markdown.parser.MarkdownParser
import me.zly2006.markdown.parser.ast.Document
import me.zly2006.markdown.parser.html.HtmlRenderer
import me.zly2006.markdown.runtime.MarkdownDirectivePipeline
import me.zly2006.markdown.runtime.MarkdownDirectivePlugin
import me.zly2006.markdown.runtime.MarkdownDirectiveRegistry

/**
 * 高层 HTML 导出入口：统一走 runtime transform pipeline，避免绕过插件导致输出不一致。
 */
object MarkdownHtml {
    fun render(
        markdown: String,
        config: MarkdownConfig = MarkdownConfig.Default,
        directivePlugins: List<MarkdownDirectivePlugin> = emptyList(),
        softBreak: String = "\n",
        escapeHtml: Boolean = false,
        xhtml: Boolean = true,
    ): String {
        val registry = MarkdownDirectiveRegistry(directivePlugins)
        val normalized = MarkdownDirectivePipeline(registry).transform(markdown).markdown

        val parser = MarkdownParser(
            flavour = config.flavour,
            customEmojiMap = config.customEmojiMap,
            enableAsciiEmoticons = config.enableAsciiEmoticons,
            enableLinting = config.enableLinting,
        )
        val document = parser.parse(normalized)

        return render(
            document = document,
            directivePlugins = directivePlugins,
            softBreak = softBreak,
            escapeHtml = escapeHtml,
            xhtml = xhtml,
        )
    }

    fun render(
        document: Document,
        directivePlugins: List<MarkdownDirectivePlugin> = emptyList(),
        softBreak: String = "\n",
        escapeHtml: Boolean = false,
        xhtml: Boolean = true,
    ): String {
        val registry = MarkdownDirectiveRegistry(directivePlugins)
        val renderer = HtmlRenderer(
            softBreak = softBreak,
            escapeHtml = escapeHtml,
            xhtml = xhtml,
            directiveBlockFallback = { node ->
                registry.findHtmlDirectiveFallback(node.tagName)?.render(node)
            },
            directiveInlineFallback = { node ->
                registry.findHtmlInlineDirectiveFallback(node.tagName)?.render(node)
            },
        )
        return renderer.render(document)
    }
}
