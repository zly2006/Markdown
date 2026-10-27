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

package me.zly2006.markdown.renderer.block

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.zly2006.markdown.parser.ast.Figure
import me.zly2006.markdown.renderer.DefaultMarkdownImage
import me.zly2006.markdown.renderer.LocalImageRenderer
import me.zly2006.markdown.renderer.LocalMarkdownTheme
import me.zly2006.markdown.renderer.MarkdownImageData

/**
 * Figure 渲染器：将 Figure 节点渲染为图片 + 标题（figcaption）。
 *
 * 居中显示图片，下方显示斜体标题文本。
 */
@Composable
internal fun FigureRenderer(
    node: Figure,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val customRenderer = LocalImageRenderer.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 渲染图片
        val imageData = MarkdownImageData(
            url = node.imageUrl,
            altText = node.caption,
            title = node.caption,
            width = node.imageWidth,
            height = node.imageHeight,
            attributes = node.attributes,
        )
        if (customRenderer != null) {
            customRenderer(imageData, Modifier)
        } else {
            DefaultMarkdownImage(data = imageData)
        }

        // 渲染 figcaption（标题）
        if (node.caption.isNotEmpty()) {
            BasicText(
                text = node.caption,
                style = theme.bodyStyle.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize = theme.bodyStyle.fontSize * 0.875f,
                    textAlign = TextAlign.Center,
                    color = theme.blockQuoteTextColor,
                ),
                modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp),
            )
        }
    }
}
