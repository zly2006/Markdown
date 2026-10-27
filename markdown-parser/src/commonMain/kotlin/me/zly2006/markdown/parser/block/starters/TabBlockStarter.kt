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
import me.zly2006.markdown.parser.ast.TabBlock
import me.zly2006.markdown.parser.ast.TabItem
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 内容标签页块开启器（MkDocs Material 风格）。
 *
 * 语法：`=== "Tab Title"`
 *
 * 标签页块由连续的 `===` 行组成，每个 `===` 行开启一个新的标签项，
 * 后续缩进 4 个空格的内容属于该标签项。
 */
internal class TabBlockStarter : BlockStarter {
    override val priority: Int = 295
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd) return null

        // 检测 === 开头
        if (cursor.peek() != '=') return null

        var eqCount = 0
        val snap = cursor.snapshot()
        while (!cursor.isAtEnd && cursor.peek() == '=') {
            cursor.advance()
            eqCount++
        }
        if (eqCount != 3) return null

        // === 后必须跟空格
        if (cursor.isAtEnd || cursor.peek() != ' ') return null
        cursor.advance() // skip space

        val rest = cursor.rest().trim()
        cursor.advance(cursor.remaining)

        // 提取引号内的标题
        val title = extractQuotedTitle(rest) ?: return null

        // 创建 TabBlock 容器
        val tabBlock = TabBlock()
        tabBlock.lineRange = LineRange(lineIdx, lineIdx + 1)

        // 创建第一个 TabItem
        val tabItem = TabItem(title = title)
        tabItem.lineRange = LineRange(lineIdx, lineIdx + 1)
        tabBlock.appendChild(tabItem)

        val ob = OpenBlock(tabBlock, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.starterTag = this::class.simpleName
        return ob
    }

    companion object {
        private val QUOTED_TITLE_REGEX = Regex("""^"([^"]*)"|^'([^']*)'""")

        fun extractQuotedTitle(text: String): String? {
            val match = QUOTED_TITLE_REGEX.find(text) ?: return null
            return match.groupValues[1].ifEmpty { match.groupValues[2] }
        }

        /** 检测一行是否是 tab item 起始行：`=== "Title"` */
        fun isTabItemLine(line: String): Boolean {
            val trimmed = line.trimStart()
            if (!trimmed.startsWith("=== ")) return false
            val rest = trimmed.removePrefix("=== ").trim()
            return extractQuotedTitle(rest) != null
        }

        /** 从 tab item 行提取标题 */
        fun extractTabTitle(line: String): String? {
            val trimmed = line.trimStart()
            if (!trimmed.startsWith("=== ")) return null
            val rest = trimmed.removePrefix("=== ").trim()
            return extractQuotedTitle(rest)
        }
    }
}
