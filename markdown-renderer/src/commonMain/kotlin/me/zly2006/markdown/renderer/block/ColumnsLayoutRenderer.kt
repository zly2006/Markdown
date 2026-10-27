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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.zly2006.markdown.parser.ast.ColumnItem
import me.zly2006.markdown.parser.ast.ColumnsLayout
import me.zly2006.markdown.renderer.MarkdownBlockChildren

/**
 * 多列布局渲染器：将 [ColumnsLayout] 渲染为水平排列的多列结构。
 *
 * 每个 [ColumnItem] 按指定宽度（或平均分配）排列。
 */
@Composable
internal fun ColumnsLayoutRenderer(
    node: ColumnsLayout,
    modifier: Modifier = Modifier,
) {
    val columns = node.children.filterIsInstance<ColumnItem>()
    if (columns.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (column in columns) {
            val weight = parseWeight(column.width, columns.size)
            MarkdownBlockChildren(
                parent = column,
                modifier = Modifier.weight(weight),
            )
        }
    }
}

/**
 * 将列宽字符串解析为 Row weight。
 *
 * - `"50%"` → 0.5f
 * - `"33.3%"` → 0.333f
 * - 空字符串 → 平均分配 `1f / columnCount`
 */
@Suppress("UNUSED_PARAMETER")
private fun parseWeight(width: String, columnCount: Int): Float {
    if (width.isBlank()) return 1f

    val percentMatch = PERCENT_REGEX.find(width)
    if (percentMatch != null) {
        val percent = percentMatch.groupValues[1].toFloatOrNull()
        if (percent != null && percent > 0) {
            return (percent / 100f).coerceIn(0.05f, 1f)
        }
    }

    return 1f
}

private val PERCENT_REGEX = Regex("""^(\d+(?:\.\d+)?)%$""")
