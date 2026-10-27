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

package me.zly2006.markdown.renderer.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

internal data class InlineFlowLayout(
    val widthPx: Float,
    val heightPx: Float,
    val firstBaselinePx: Float? = null,
    val lastBaselinePx: Float? = null,
    val lines: List<InlineFlowLine>,
)

internal data class InlineFlowLine(
    val textStyle: TextStyle,
    val lineWidthPx: Float,
    val lineHeightPx: Float,
    val baselinePx: Float,
    val textAlign: TextAlign,
    val items: List<LineItem>,
)

internal sealed class LineItem {
    abstract val widthPx: Float
    abstract val heightPx: Float

    data class TextItem(
        val text: AnnotatedString,
        override val widthPx: Float,
        override val heightPx: Float,
        val baselinePx: Float,
    ) : LineItem()

    data class InlineItem(
        val id: String,
        override val widthPx: Float,
        override val heightPx: Float,
        /**
         * 行内内容自身的宽度。行内排版无法切开单个行内项，所以超宽时 [widthPx] 会被夹到可用行宽，
         * 而这里保留真实宽度，渲染侧据此判断是否需要提供横向滚动。
         */
        val naturalWidthPx: Float,
        val alternateText: String,
        val content: @Composable () -> Unit,
    ) : LineItem()
}

internal sealed class Token {
    data class Text(
        val annotated: AnnotatedString,
    ) : Token()

    data class Inline(
        val id: String,
    ) : Token()

    data object Newline : Token()
}
