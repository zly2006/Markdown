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

package me.zly2006.markdown.parser.block.postprocessors

import me.zly2006.markdown.parser.ast.ContainerNode
import me.zly2006.markdown.parser.ast.Document
import me.zly2006.markdown.parser.ast.Emoji
import me.zly2006.markdown.parser.ast.EscapedChar
import me.zly2006.markdown.parser.ast.HardLineBreak
import me.zly2006.markdown.parser.ast.Heading
import me.zly2006.markdown.parser.ast.HtmlEntity
import me.zly2006.markdown.parser.ast.InlineCode
import me.zly2006.markdown.parser.ast.Node
import me.zly2006.markdown.parser.ast.SetextHeading
import me.zly2006.markdown.parser.ast.SoftLineBreak
import me.zly2006.markdown.parser.ast.Text

/**
 * 自动为所有标题生成 ID（slug），基于标题文本内容。
 */
class HeadingIdProcessor : PostProcessor {
    override val priority: Int = 100

    override fun process(document: Document) {
        val usedIds = mutableMapOf<String, Int>()
        for (child in document.children) {
            processRecursive(child, usedIds)
        }
    }

    private fun processRecursive(node: Node, usedIds: MutableMap<String, Int>) {
        when (node) {
            is Heading -> {
                if (node.customId == null) {
                    val text = extractPlainText(node)
                    val slug = generateSlug(text)
                    node.autoId = deduplicateId(slug, usedIds)
                } else {
                    usedIds[node.customId!!] = (usedIds[node.customId!!] ?: 0) + 1
                }
            }
            is SetextHeading -> {
                val text = extractPlainText(node)
                val slug = generateSlug(text)
                node.autoId = deduplicateId(slug, usedIds)
            }
            is ContainerNode -> {
                for (child in node.children) {
                    processRecursive(child, usedIds)
                }
            }
            else -> {}
        }
    }

    companion object {
        fun extractPlainText(node: Node): String = when (node) {
            is Text -> node.literal
            is InlineCode -> node.literal
            is Emoji -> node.literal
            is EscapedChar -> node.literal
            is HtmlEntity -> node.resolved.ifEmpty { node.literal }
            is SoftLineBreak -> " "
            is HardLineBreak -> " "
            is ContainerNode -> node.children.joinToString("") { extractPlainText(it) }
            else -> ""
        }

        fun generateSlug(text: String): String = text
            .lowercase()
            .replace(Regex("[^\\w\\u4e00-\\u9fff-]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .ifEmpty { "heading" }

        fun deduplicateId(slug: String, usedIds: MutableMap<String, Int>): String {
            val count = usedIds[slug]
            return if (count == null) {
                usedIds[slug] = 1
                slug
            } else {
                usedIds[slug] = count + 1
                val newId = "$slug-$count"
                usedIds[newId] = 1
                newId
            }
        }
    }
}
