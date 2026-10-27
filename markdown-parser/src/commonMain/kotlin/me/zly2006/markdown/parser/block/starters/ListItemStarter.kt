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
import me.zly2006.markdown.parser.ast.ListItem
import me.zly2006.markdown.parser.block.ListItemMeta
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 列表项开启器。
 * 支持无序列表（`-`, `*`, `+`）和有序列表（`1.`, `1)`）。
 * 包含任务列表检测 (`[ ]`, `[x]`)。
 */
internal class ListItemStarter : BlockStarter {
    override val priority: Int = 500
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        val snap = cursor.snapshot()
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd) return null

        val c = cursor.peek()
        var ordered = false
        var bulletChar = c
        var startNumber = 1
        var delimiter = '.'
        var markerWidth = 0

        when (c) {
            '-', '*', '+' -> {
                cursor.advance()
                markerWidth = 1
            }
            in '0'..'9' -> {
                ordered = true
                val numBuilder = StringBuilder()
                while (!cursor.isAtEnd && cursor.peek() in '0'..'9' && numBuilder.length < 9) {
                    numBuilder.append(cursor.advance())
                }
                if (cursor.isAtEnd) return null
                val d = cursor.peek()
                if (d != '.' && d != ')') return null
                delimiter = d
                cursor.advance()
                startNumber = numBuilder.toString().toIntOrNull() ?: return null
                markerWidth = numBuilder.length + 1
            }
            else -> return null
        }

        if (!cursor.isAtEnd && cursor.peek() != ' ' && cursor.peek() != '\t') return null

        val contentIndent: Int
        if (cursor.isAtEnd) {
            contentIndent = indent + markerWidth + 1
        } else {
            val preSpaceSnap = cursor.snapshot()
            val postMarker = cursor.advanceSpaces(4)
            if (cursor.isAtEnd || cursor.restIsBlank()) {
                // blank line after marker: use minimum indent
                cursor.restore(preSpaceSnap)
                if (!cursor.isAtEnd && (cursor.peek() == ' ' || cursor.peek() == '\t')) {
                    cursor.advanceSpaces(1)
                }
                contentIndent = indent + markerWidth + 1
            } else if (!cursor.isAtEnd && (cursor.peek() == ' ' || cursor.peek() == '\t')) {
                // more than 4 columns of whitespace after marker -> indented code content; use minimum indent
                cursor.restore(preSpaceSnap)
                cursor.advanceSpaces(1)
                contentIndent = indent + markerWidth + 1
            } else {
                contentIndent = indent + markerWidth + if (postMarker == 0) 1 else postMarker
            }
        }

        var isTask = false
        var checked = false
        if (!cursor.isAtEnd) {
            val taskSnap = cursor.snapshot()
            if (cursor.peek() == '[') {
                cursor.advance()
                if (!cursor.isAtEnd) {
                    val mark = cursor.peek()
                    if (mark == ' ' || mark == 'x' || mark == 'X') {
                        cursor.advance()
                        if (!cursor.isAtEnd && cursor.peek() == ']') {
                            cursor.advance()
                            if (cursor.isAtEnd || cursor.peek() == ' ' || cursor.peek() == '\t') {
                                isTask = true
                                checked = mark == 'x' || mark == 'X'
                                if (!cursor.isAtEnd) cursor.advance()
                            } else {
                                cursor.restore(taskSnap)
                            }
                        } else {
                            cursor.restore(taskSnap)
                        }
                    } else {
                        cursor.restore(taskSnap)
                    }
                } else {
                    cursor.restore(taskSnap)
                }
            }
        }

        val listItem = ListItem(
            markerIndent = indent,
            contentIndent = contentIndent,
        )
        listItem.taskListItem = isTask
        listItem.checked = checked
        listItem.lineRange = LineRange(lineIdx, lineIdx + 1)

        val ob = OpenBlock(listItem, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.listItemMeta = ListItemMeta(ordered, bulletChar, startNumber, delimiter)
        ob.starterTag = this::class.simpleName
        return ob
    }
}
