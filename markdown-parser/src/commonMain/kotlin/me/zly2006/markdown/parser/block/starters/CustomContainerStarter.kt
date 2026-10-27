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

package me.zly2006.markdown.parser.block.starters

import me.zly2006.markdown.parser.LineRange
import me.zly2006.markdown.parser.ast.CustomContainer
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.AttributeParser
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 自定义容器块开启器：`::: type "title" {.class #id}` 到 `:::`。
 */
internal class CustomContainerStarter : BlockStarter {
    override val priority: Int = 300
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd) return null

        if (cursor.peek() != ':') return null

        var colonCount = 0
        val snap = cursor.snapshot()
        while (!cursor.isAtEnd && cursor.peek() == ':') {
            cursor.advance()
            colonCount++
        }
        if (colonCount < 3) return null

        val rest = cursor.rest().trim()
        cursor.advance(cursor.remaining)

        var type = ""
        var title = ""
        var cssClasses = emptyList<String>()
        var cssId: String? = null

        if (rest.isNotEmpty()) {
            // extract {.class #id key=value} block via shared parser
            val (attrs, afterAttrs) = AttributeParser.parse(rest)
            cssClasses = attrs.classes
            cssId = attrs.id

            // extract quoted title from what's left
            var remaining = afterAttrs
            val titleMatch = CONTAINER_TITLE_REGEX.find(remaining)
            if (titleMatch != null) {
                title = titleMatch.groupValues[1].ifEmpty { titleMatch.groupValues[2] }
                remaining = remaining.removeRange(titleMatch.range).trim()
            }

            type = remaining.trim().split("\\s+".toRegex()).firstOrNull() ?: ""
        }

        val block = CustomContainer(
            type = type,
            title = title,
            cssClasses = cssClasses,
            cssId = cssId,
        )
        block.lineRange = LineRange(lineIdx, lineIdx + 1)

        val ob = OpenBlock(block, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.fenceChar = ':'
        ob.fenceLength = colonCount
        ob.fenceIndent = indent
        ob.starterTag = this::class.simpleName
        return ob
    }

    companion object {
        private val CONTAINER_TITLE_REGEX = Regex("\"([^\"]*)\"|'([^']*)'")
    }
}
