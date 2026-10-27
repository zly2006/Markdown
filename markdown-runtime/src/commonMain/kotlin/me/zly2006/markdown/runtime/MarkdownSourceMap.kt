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

package me.zly2006.markdown.runtime

/**
 * 输入转换后的源码映射信息。
 *
 * 当前版本先保留结构，为后续诊断和源码回跳打基础。
 */
sealed interface MarkdownSourceMap {
    data object Identity : MarkdownSourceMap

    data class Segmented(
        val segments: List<Segment>,
    ) : MarkdownSourceMap {
        data class Segment(
            val outputStart: Int,
            val outputEnd: Int,
            val inputStart: Int,
            val inputEnd: Int,
        )
    }
}

internal fun composeSourceMap(
    previous: MarkdownSourceMap,
    current: MarkdownSourceMap,
): MarkdownSourceMap {
    if (previous is MarkdownSourceMap.Identity) return current
    if (current is MarkdownSourceMap.Identity) return previous
    // 先保留最后一次非 Identity 映射，避免过度承诺尚未接入的精确偏移合成。
    return current
}
