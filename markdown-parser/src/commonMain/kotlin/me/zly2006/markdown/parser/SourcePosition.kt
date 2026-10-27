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

package me.zly2006.markdown.parser

/**
 * 表示源文本中的一个位置。
 */
data class SourcePosition(
    val line: Int,
    val column: Int,
    val offset: Int,
) : Comparable<SourcePosition> {
    override fun compareTo(other: SourcePosition): Int = offset.compareTo(other.offset)

    companion object {
        val ZERO = SourcePosition(0, 0, 0)
    }
}

/**
 * 表示源文本中的一个范围（起始位置包含，结束位置不包含）。
 */
data class SourceRange(
    val start: SourcePosition,
    val end: SourcePosition,
) {
    val length: Int get() = end.offset - start.offset

    fun contains(offset: Int): Boolean = offset in start.offset until end.offset

    fun overlaps(other: SourceRange): Boolean =
        start.offset < other.end.offset && other.start.offset < end.offset

    fun shift(linesDelta: Int, offsetDelta: Int): SourceRange = SourceRange(
        start = SourcePosition(start.line + linesDelta, start.column, start.offset + offsetDelta),
        end = SourcePosition(end.line + linesDelta, end.column, end.offset + offsetDelta),
    )

    companion object {
        val EMPTY = SourceRange(SourcePosition.ZERO, SourcePosition.ZERO)
    }
}

/**
 * 表示行范围（起始行包含，结束行不包含）。
 */
data class LineRange(
    val startLine: Int,
    val endLine: Int,
) {
    val lineCount: Int get() = endLine - startLine

    fun contains(line: Int): Boolean = line in startLine until endLine

    fun overlaps(other: LineRange): Boolean =
        startLine < other.endLine && other.startLine < endLine

    fun shift(delta: Int): LineRange = LineRange(startLine + delta, endLine + delta)

    fun expand(other: LineRange): LineRange = LineRange(
        minOf(startLine, other.startLine),
        maxOf(endLine, other.endLine),
    )
}
