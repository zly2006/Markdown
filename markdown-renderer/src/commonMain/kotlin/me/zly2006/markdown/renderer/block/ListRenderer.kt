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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.zly2006.markdown.parser.ast.BlankLine
import me.zly2006.markdown.parser.ast.Heading
import me.zly2006.markdown.parser.ast.Image
import me.zly2006.markdown.parser.ast.ListBlock
import me.zly2006.markdown.parser.ast.ListItem
import me.zly2006.markdown.parser.ast.Paragraph
import me.zly2006.markdown.parser.ast.SetextHeading
import me.zly2006.markdown.renderer.DeferredMarkdownColumn
import me.zly2006.markdown.renderer.LocalMarkdownTheme

/**
 * 列表渲染器（有序/无序列表）。
 */
@Composable
internal fun ListBlockRenderer(
    node: ListBlock,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val spacing = if (node.tight) 2.dp else theme.blockSpacing

    val items = node.children.filterIsInstance<ListItem>()
    val markerWidth = if (node.ordered) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val digitCount = maxOf(node.startNumber.toString().length, (node.startNumber + items.lastIndex).toString().length)
        // 为整个列表预留统一编号宽度，不对惰性列表中的每一项逐个测量。
        val widthPx = remember(digitCount, theme.bodyStyle, textMeasurer) {
            val digitWidth = (0..9).maxOf { digit ->
                textMeasurer.measure(digit.toString(), style = theme.bodyStyle).size.width
            }
            digitWidth * digitCount + textMeasurer.measure(".", style = theme.bodyStyle).size.width
        }
        (with(density) { widthPx.toDp() } + 4.dp).coerceAtLeast(24.dp)
    } else {
        16.dp
    }
    DeferredMarkdownColumn(
        blocks = items,
        modifier = modifier.fillMaxWidth(),
        blockSpacing = spacing,
        prefetchViewports = 0.5f,
    ) { index, item ->
        ListItemRenderer(
            node = item,
            ordered = node.ordered,
            index = node.startNumber + index,
            markerWidth = markerWidth,
        )
    }
}

@Composable
private fun ListItemRenderer(
    node: ListItem,
    ordered: Boolean,
    index: Int,
    markerWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val firstBlock = node.children.firstOrNull { it !is BlankLine }
    val startsWithText = when (firstBlock) {
        is Paragraph -> firstBlock.children.firstOrNull() !is Image
        is Heading, is SetextHeading -> true
        else -> false
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(start = theme.listIndent),
        horizontalArrangement = Arrangement.Start,
    ) {
        val markerAlignment = if (startsWithText) Modifier.alignByBaseline() else Modifier.align(Alignment.Top)
        // Marker 列
        when {
            node.taskListItem -> {
                Checkbox(
                    checked = node.checked,
                    onCheckedChange = null,
                    modifier = Modifier.size(20.dp).align(Alignment.Top),
                    colors = CheckboxDefaults.colors(
                        checkedColor = theme.taskCheckedColor,
                        uncheckedColor = theme.taskUncheckedColor,
                    ),
                )
            }
            ordered -> {
                BasicText(
                    text = "$index.",
                    modifier = Modifier.width(markerWidth).padding(end = 4.dp).then(markerAlignment),
                    style = theme.bodyStyle.copy(color = theme.listBulletColor, textAlign = TextAlign.End),
                )
            }
            else -> {
                BasicText(
                    text = "•",
                    modifier = Modifier.width(markerWidth).then(markerAlignment),
                    style = theme.bodyStyle.copy(color = theme.listBulletColor),
                )
            }
        }

        // 内容列
        Box(modifier = Modifier.weight(1f).then(markerAlignment)) {
            Column(verticalArrangement = Arrangement.spacedBy(theme.blockSpacing)) {
                node.children.filterNot { it is BlankLine }.forEach { child ->
                    key(child.stableKey, blockRenderRevision(child)) {
                        BlockRenderer(
                            node = child,
                            renderRevision = blockRenderRevision(child),
                        )
                    }
                }
            }
        }
    }
}
