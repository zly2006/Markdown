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
import me.zly2006.markdown.parser.ast.DefinitionDescription
import me.zly2006.markdown.parser.ast.DefinitionList
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 定义描述开启器：`: description text`。
 * 必须在段落（作为术语）或已存在的定义列表之后。
 */
internal class DefinitionDescriptionStarter : BlockStarter {
    override val priority: Int = 520
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        if (tip.paragraphContent == null && tip.node !is DefinitionList) return null

        val snap = cursor.snapshot()
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd || cursor.peek() != ':') {
            cursor.restore(snap)
            return null
        }
        cursor.advance()
        if (cursor.isAtEnd || (cursor.peek() != ' ' && cursor.peek() != '\t')) {
            cursor.restore(snap)
            return null
        }
        cursor.advance()

        val desc = DefinitionDescription()
        desc.lineRange = LineRange(lineIdx, lineIdx + 1)

        val ob = OpenBlock(desc, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.starterTag = this::class.simpleName
        return ob
    }
}
