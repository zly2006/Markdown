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
import me.zly2006.markdown.parser.ast.Table
import me.zly2006.markdown.parser.ast.TableBody
import me.zly2006.markdown.parser.ast.TableCell
import me.zly2006.markdown.parser.ast.TableHead
import me.zly2006.markdown.parser.ast.TableRow
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.block.TableParser
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 表格开启器（GFM 扩展）。
 * 检测段落后跟的分隔行 `| --- | --- |`。
 */
internal class TableStarter : BlockStarter {
    override val priority: Int = 200
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        if (tip.paragraphContent == null) return null
        val headerLine = tip.paragraphContent.toString().trim()
        if (!headerLine.contains('|')) return null

        val delimLine = cursor.rest().trim()
        val alignments = TableParser.parseTableDelimiterRow(delimLine) ?: return null
        val headerCells = TableParser.parseTableRow(headerLine)
        if (headerCells.isEmpty()) return null

        val table = Table()
        table.columnAlignments = alignments
        table.lineRange = LineRange(tip.contentStartLine, lineIdx + 1)

        val head = TableHead()
        val headerRow = TableRow()
        val colCount = alignments.size
        for (i in 0 until colCount) {
            val cellContent = headerCells.getOrElse(i) { "" }
            val align = alignments[i]
            val cell = TableCell(alignment = align, isHeader = true)
            cell.lineRange = LineRange(tip.contentStartLine, tip.contentStartLine + 1)
            cell.rawContent = cellContent
            headerRow.appendChild(cell)
        }
        head.appendChild(headerRow)
        table.appendChild(head)

        val body = TableBody()
        table.appendChild(body)

        val ob = OpenBlock(table, contentStartLine = tip.contentStartLine, lastLineIndex = lineIdx)
        ob.contentLines.clear()
        ob.starterTag = this::class.simpleName
        return ob
    }
}
