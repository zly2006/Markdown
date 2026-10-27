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
import me.zly2006.markdown.parser.ast.PageBreak
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 分页符开启器：`***pagebreak***`。
 *
 * 识别独占一行的 `***pagebreak***` 标记，生成 [PageBreak] AST 节点。
 * 用于 PDF 导出/打印场景。
 */
internal class PageBreakStarter : BlockStarter {
    override val priority: Int = 205
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd) return null

        val rest = cursor.rest().trim()
        if (!PAGEBREAK_REGEX.matches(rest)) return null

        cursor.advance(cursor.remaining)

        val node = PageBreak()
        node.lineRange = LineRange(lineIdx, lineIdx + 1)

        return OpenBlock(node, contentStartLine = lineIdx, lastLineIndex = lineIdx).also {
            it.starterTag = this::class.simpleName
        }
    }

    companion object {
        private val PAGEBREAK_REGEX = Regex("^\\*{3}pagebreak\\*{3}$", RegexOption.IGNORE_CASE)
    }
}
