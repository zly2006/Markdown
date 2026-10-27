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
import me.zly2006.markdown.parser.ast.HtmlBlock
import me.zly2006.markdown.parser.ast.InlineHtml
import me.zly2006.markdown.parser.ast.Node

/**
 * GFM：过滤禁止的原始 HTML 标签。
 */
class HtmlFilterProcessor : PostProcessor {
    override val priority: Int = 200

    override fun process(document: Document) {
        processRecursive(document)
    }

    private fun processRecursive(node: Node) {
        when (node) {
            is HtmlBlock -> {
                val filtered = filterGfmDisallowedTags(node.literal)
                if (filtered != node.literal) {
                    node.literal = filtered
                }
            }
            is InlineHtml -> {
                val filtered = filterGfmDisallowedTags(node.literal)
                if (filtered != node.literal) {
                    node.literal = filtered
                }
            }
            is ContainerNode -> {
                for (child in node.children.toList()) {
                    processRecursive(child)
                }
            }
            else -> {}
        }
    }

    private fun filterGfmDisallowedTags(html: String): String = GFM_DISALLOWED_TAG_REGEX.replace(html) { match ->
        "<!-- ${match.value} (filtered) -->"
    }

    companion object {
        private val GFM_DISALLOWED_TAG_REGEX = Regex(
            "<(title|textarea|style|xmp|iframe|noembed|noframes|script|plaintext)(\\s[^>]*)?>",
            RegexOption.IGNORE_CASE,
        )
    }
}
