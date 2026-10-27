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
import me.zly2006.markdown.parser.ast.FootnoteDefinition
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 脚注定义开启器：`[^label]: content`。
 */
internal class FootnoteDefinitionStarter : BlockStarter {
    override val priority: Int = 510
    override val canInterruptParagraph: Boolean = false

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd || cursor.peek() != '[') return null
        cursor.advance()
        if (cursor.isAtEnd || cursor.peek() != '^') return null
        cursor.advance()

        val label = StringBuilder()
        while (!cursor.isAtEnd && cursor.peek() != ']') {
            label.append(cursor.advance())
        }
        if (cursor.isAtEnd || label.isEmpty()) return null
        cursor.advance() // skip ']'
        if (cursor.isAtEnd || cursor.peek() != ':') return null
        cursor.advance() // skip ':'

        if (!cursor.isAtEnd && cursor.peek() == ' ') cursor.advance()

        val fd = FootnoteDefinition(label = label.toString())
        fd.lineRange = LineRange(lineIdx, lineIdx + 1)

        val ob = OpenBlock(fd, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.starterTag = this::class.simpleName
        return ob
    }
}
