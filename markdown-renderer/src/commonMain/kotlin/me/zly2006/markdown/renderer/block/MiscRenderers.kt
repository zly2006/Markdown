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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.zly2006.markdown.parser.ast.BlankLine
import me.zly2006.markdown.parser.ast.DefinitionDescription
import me.zly2006.markdown.parser.ast.DefinitionList
import me.zly2006.markdown.parser.ast.DefinitionTerm
import me.zly2006.markdown.parser.ast.FootnoteDefinition
import me.zly2006.markdown.parser.ast.HtmlBlock
import me.zly2006.markdown.parser.ast.Node
import me.zly2006.markdown.parser.ast.Paragraph
import me.zly2006.markdown.renderer.LocalFootnoteNavigationState
import me.zly2006.markdown.renderer.LocalMarkdownTheme
import me.zly2006.markdown.renderer.LocalOnFootnoteBackClick
import me.zly2006.markdown.renderer.MarkdownBlockChildren
import me.zly2006.markdown.renderer.inline.InlineFlowText
import me.zly2006.markdown.renderer.inline.rememberInlineContent

/**
 * HTML 块渲染器：以等宽字体显示原始 HTML。
 */
@Composable
internal fun HtmlBlockRenderer(
    node: HtmlBlock,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    Text(
        text = node.literal.trimEnd('\n'),
        modifier = modifier.fillMaxWidth(),
        style = theme.codeBlockStyle.copy(fontFamily = FontFamily.Monospace),
    )
}

/**
 * 定义列表渲染器。
 */
@Composable
internal fun DefinitionListRenderer(
    node: DefinitionList,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (child in node.children) {
            when (child) {
                is DefinitionTerm -> {
                    val inlineResult = rememberInlineContent(
                        parent = child,
                        hostTextStyle = theme.bodyStyle.copy(fontWeight = FontWeight.Bold),
                    )
                    InlineFlowText(
                        annotated = inlineResult.annotated,
                        inlineContents = inlineResult.inlineContents,
                        style = theme.bodyStyle.copy(fontWeight = FontWeight.Bold),
                    )
                }
                is DefinitionDescription -> {
                    MarkdownBlockChildren(
                        parent = child,
                        modifier = Modifier.padding(start = 24.dp),
                    )
                }
                else -> BlockRenderer(child)
            }
        }
    }
}

/**
 * 脚注定义渲染器。
 */
@Composable
internal fun FootnoteDefinitionRenderer(
    node: FootnoteDefinition,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val footnoteNavigationState = LocalFootnoteNavigationState.current
    val onFootnoteBackClick = LocalOnFootnoteBackClick.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    DisposableEffect(footnoteNavigationState, node.label, bringIntoViewRequester) {
        footnoteNavigationState?.registerDefinition(node.label, bringIntoViewRequester)
        onDispose {
            footnoteNavigationState?.unregisterDefinition(node.label, bringIntoViewRequester)
        }
    }

    val contentBlocks = remember(node) {
        node.children.filter { it !is BlankLine }
    }
    val firstBlock = contentBlocks.firstOrNull()
    val remainingBlocks = if (contentBlocks.size > 1) contentBlocks.drop(1) else emptyList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(top = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "[${node.index}]",
                style = theme.bodyStyle.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = theme.footnoteStyle.fontSize,
                ),
            )
            Text(
                text = "↩",
                modifier = Modifier
                    .then(
                        if (onFootnoteBackClick != null) {
                            Modifier.clickable { onFootnoteBackClick(node.label) }
                        } else {
                            Modifier
                        },
                    ),
                style = theme.bodyStyle.copy(
                    color = theme.linkColor,
                    fontSize = theme.footnoteStyle.fontSize,
                ),
            )
            Box(modifier = Modifier.weight(1f)) {
                if (firstBlock != null) {
                    key(firstBlock::class, firstBlock.stableKey) {
                        FootnoteContentBlock(firstBlock)
                    }
                }
            }
        }
        if (remainingBlocks.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(theme.blockSpacing),
            ) {
                for (child in remainingBlocks) {
                    key(child::class, child.stableKey) {
                        FootnoteContentBlock(child)
                    }
                }
            }
        }
    }
}

@Composable
private fun FootnoteContentBlock(node: Node) {
    when (node) {
        is Paragraph -> ParagraphRenderer(node, Modifier.fillMaxWidth())
        else -> BlockRenderer(
            node = node,
            renderRevision = blockRenderRevision(node),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
