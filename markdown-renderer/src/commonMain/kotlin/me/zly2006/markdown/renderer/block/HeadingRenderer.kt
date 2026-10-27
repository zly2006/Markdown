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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import me.zly2006.markdown.parser.ast.Heading
import me.zly2006.markdown.parser.ast.Node
import me.zly2006.markdown.parser.ast.SetextHeading
import me.zly2006.markdown.renderer.LocalMarkdownConfig
import me.zly2006.markdown.renderer.LocalMarkdownTheme
import me.zly2006.markdown.renderer.LocalOnLinkClick
import me.zly2006.markdown.renderer.LocalRendererDocument
import me.zly2006.markdown.renderer.inline.InlineFlowText
import me.zly2006.markdown.renderer.inline.rememberInlineContent

/**
 * ATX 标题渲染器 (# ~ ######)
 */
@Composable
internal fun HeadingRenderer(
    node: Heading,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val onLinkClick = LocalOnLinkClick.current
    val config = LocalMarkdownConfig.current
    val level = (node.level - 1).coerceIn(0, theme.headingStyles.lastIndex)
    val style = theme.headingStyles[level]
    val inlineResult = rememberInlineContent(
        parent = node,
        onLinkClick = onLinkClick,
        hostTextStyle = style,
    )
    val numbering = if (config.enableHeadingNumbering) {
        val document = LocalRendererDocument.current
        remember(document, node) { computeHeadingNumber(document.children, node) }
    } else {
        null
    }

    val finalAnnotated = if (numbering != null) {
        remember(numbering, inlineResult.annotated) {
            buildAnnotatedString {
                append("$numbering ")
                append(inlineResult.annotated)
            }
        }
    } else {
        inlineResult.annotated
    }

    Column(modifier = modifier.fillMaxWidth()) {
        InlineFlowText(
            annotated = finalAnnotated,
            inlineContents = inlineResult.inlineContents,
            style = style,
        )

        // h1 和 h2 下方添加分隔线（GitHub 风格）
        if (node.level <= 2) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                thickness = theme.dividerThickness,
                color = theme.dividerColor,
            )
        }
    }
}

/**
 * Setext 标题渲染器 (=== / ---)
 */
@Composable
internal fun SetextHeadingRenderer(
    node: SetextHeading,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val onLinkClick = LocalOnLinkClick.current
    val config = LocalMarkdownConfig.current
    val level = (node.level - 1).coerceIn(0, theme.headingStyles.lastIndex)
    val style = theme.headingStyles[level]
    val inlineResult = rememberInlineContent(
        parent = node,
        onLinkClick = onLinkClick,
        hostTextStyle = style,
    )
    val numbering = if (config.enableHeadingNumbering) {
        val document = LocalRendererDocument.current
        remember(document, node) { computeHeadingNumberForSetext(document.children, node) }
    } else {
        null
    }

    val finalAnnotated = if (numbering != null) {
        remember(numbering, inlineResult.annotated) {
            buildAnnotatedString {
                append("$numbering ")
                append(inlineResult.annotated)
            }
        }
    } else {
        inlineResult.annotated
    }

    Column(modifier = modifier.fillMaxWidth()) {
        InlineFlowText(
            annotated = finalAnnotated,
            inlineContents = inlineResult.inlineContents,
            style = style,
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = theme.dividerThickness,
            color = theme.dividerColor,
        )
    }
}

/**
 * 计算 ATX 标题的层级编号（如 "1", "1.1", "1.1.2"）。
 * 遍历文档顶层子节点，依次递增各级计数器。
 */
private fun computeHeadingNumber(children: List<Node>, target: Heading): String? {
    // 维护 6 级计数器
    val counters = IntArray(6)
    for (child in children) {
        val level = when (child) {
            is Heading -> child.level
            is SetextHeading -> child.level
            else -> continue
        }
        val idx = (level - 1).coerceIn(0, 5)
        counters[idx]++
        // 重置下级计数器
        for (i in idx + 1..5) counters[i] = 0

        if (child === target) {
            return buildNumberString(counters, idx)
        }
    }
    return null
}

private fun computeHeadingNumberForSetext(children: List<Node>, target: SetextHeading): String? {
    val counters = IntArray(6)
    for (child in children) {
        val level = when (child) {
            is Heading -> child.level
            is SetextHeading -> child.level
            else -> continue
        }
        val idx = (level - 1).coerceIn(0, 5)
        counters[idx]++
        for (i in idx + 1..5) counters[i] = 0

        if (child === target) {
            return buildNumberString(counters, idx)
        }
    }
    return null
}

private fun buildNumberString(counters: IntArray, maxIdx: Int): String {
    val parts = mutableListOf<Int>()
    for (i in 0..maxIdx) {
        parts.add(counters[i].coerceAtLeast(1))
    }
    return parts.joinToString(".")
}
