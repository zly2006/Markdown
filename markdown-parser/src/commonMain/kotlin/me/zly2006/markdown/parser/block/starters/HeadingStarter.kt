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
import me.zly2006.markdown.parser.ast.Heading
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * ATX 标题开启器：`# heading` 到 `###### heading`。
 */
internal class HeadingStarter : BlockStarter {
    override val priority: Int = 110
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd || cursor.peek() != '#') return null

        var level = 0
        while (!cursor.isAtEnd && cursor.peek() == '#') {
            cursor.advance()
            level++
        }
        if (level > 6) return null
        if (!cursor.isAtEnd && cursor.peek() != ' ' && cursor.peek() != '\t') {
            if (!cursor.isAtEnd) return null
        }

        if (!cursor.isAtEnd && (cursor.peek() == ' ' || cursor.peek() == '\t')) {
            cursor.advance()
        }

        var content = cursor.rest().trimEnd()
        val customId = extractCustomId(content)
        if (customId != null) {
            content = content.replace(CUSTOM_ID_STRIP_REGEX, "").trimEnd()
        }
        if (content.endsWith('#')) {
            val strippedTrailing = content.trimEnd('#')
            if (strippedTrailing.isEmpty() || strippedTrailing.endsWith(' ') || strippedTrailing.endsWith('\t')) {
                content = strippedTrailing.trimEnd()
            }
        }

        val heading = Heading(level)
        heading.customId = customId
        heading.lineRange = LineRange(lineIdx, lineIdx + 1)

        val ob = OpenBlock(heading, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.contentLines.add(content)
        ob.starterTag = this::class.simpleName
        return ob
    }

    companion object {
        internal val CUSTOM_ID_REGEX = Regex("\\{#([^\\}]+)\\}\\s*$")
        internal val CUSTOM_ID_STRIP_REGEX = Regex("\\s*\\{#[^\\}]+\\}\\s*$")

        fun extractCustomId(content: String): String? {
            val match = CUSTOM_ID_REGEX.find(content) ?: return null
            return match.groupValues[1]
        }
    }
}
