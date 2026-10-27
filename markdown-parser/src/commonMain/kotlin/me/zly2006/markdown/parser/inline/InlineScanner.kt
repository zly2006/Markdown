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

package me.zly2006.markdown.parser.inline

/**
 * 行内扫描器：逐字符扫描行内内容字符串。
 *
 * 从 InlineParser.kt 提取为独立文件，供 InlineParserInstance 使用。
 */
internal class InlineScanner(
    val text: String,
) {
    var pos: Int = 0

    val isAtEnd: Boolean get() = pos >= text.length

    fun peek(): Char = if (pos < text.length) text[pos] else '\u0000'

    fun peek(offset: Int): Char {
        val idx = pos + offset
        return if (idx in text.indices) text[idx] else '\u0000'
    }

    fun advance(): Char {
        if (pos >= text.length) return '\u0000'
        return text[pos++]
    }
}
