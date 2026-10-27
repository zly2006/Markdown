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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.zly2006.markdown.parser.ast.CustomContainer
import me.zly2006.markdown.renderer.LocalMarkdownTheme
import me.zly2006.markdown.renderer.MarkdownBlockChildren

/**
 * 自定义容器渲染器 (::: type ... :::)。
 *
 * 如果容器类型匹配已知的 Admonition 样式（NOTE/TIP/WARNING 等），
 * 则使用 Admonition 风格渲染；否则使用通用容器样式。
 */
@Composable
internal fun CustomContainerRenderer(
    node: CustomContainer,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current

    // 尝试匹配 Admonition 样式
    val admonitionStyle = theme.admonitionStyles[node.type.uppercase()]

    val borderColor = admonitionStyle?.borderColor ?: Color(0xFF8B949E)
    val backgroundColor = admonitionStyle?.backgroundColor ?: Color(0xFFF6F8FA)
    val iconText = admonitionStyle?.iconText ?: "📦"
    val titleColor = admonitionStyle?.titleColor ?: Color(0xFF1F2328)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4.dp.toPx(),
                )
            }.padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
    ) {
        // 标题行（如果有类型名或标题）
        val displayTitle = node.title.ifEmpty {
            node.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        if (displayTitle.isNotEmpty()) {
            Row {
                Text(
                    text = iconText,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = displayTitle,
                    style = theme.bodyStyle.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                    ),
                )
            }
        }

        // 内容
        if (node.children.isNotEmpty()) {
            MarkdownBlockChildren(
                parent = node,
                modifier = Modifier.padding(top = if (displayTitle.isNotEmpty()) 8.dp else 0.dp),
            )
        }
    }
}
