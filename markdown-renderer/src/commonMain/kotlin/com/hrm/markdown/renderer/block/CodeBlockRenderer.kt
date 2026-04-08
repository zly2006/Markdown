package com.hrm.markdown.renderer.block

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.hrm.codehigh.renderer.CodeBlock
import com.hrm.codehigh.theme.LocalCodeTheme
import com.hrm.markdown.parser.ast.FencedCodeBlock
import com.hrm.markdown.parser.ast.IndentedCodeBlock
import com.hrm.markdown.renderer.LocalCodeHighlightTheme
import com.hrm.markdown.renderer.LocalMarkdownTheme

/**
 * 围栏代码块渲染器 (``` 或 ~~~)
 *
 * 支持通过 info-string 的 `{...}` 属性语法控制：
 * - **title**: 标题栏，如 `{title="main.kt"}`
 * - **linenos / lineNumbers**: 行号显示
 * - **highlight / hl_lines**: 高亮指定行，如 `{highlight="2,5-7"}`
 */
@Composable
internal fun FencedCodeBlockRenderer(
    node: FencedCodeBlock,
    modifier: Modifier = Modifier,
) {
    CodeBlockText(
        text = node.literal.ifEmpty { " " },
        language = node.language,
        title = node.attributes.pairs["title"],
        showLineNumbers = node.showLineNumbers,
        startLine = node.startLineNumber,
        highlightedLines = node.highlightLines.flattenLineNumbers(),
        modifier = modifier,
    )
}

/**
 * 缩进代码块渲染器
 */
@Composable
internal fun IndentedCodeBlockRenderer(
    node: IndentedCodeBlock,
    modifier: Modifier = Modifier,
) {
    CodeBlockText(
        text = node.literal.ifEmpty { " " },
        language = "",
        title = null,
        showLineNumbers = true,
        startLine = 1,
        highlightedLines = emptySet(),
        modifier = modifier,
    )
}

private fun List<IntRange>.flattenLineNumbers(): Set<Int> = buildSet {
    for (range in this@flattenLineNumbers) {
        addAll(range)
    }
}

@Composable
private fun CodeBlockText(
    text: String,
    language: String,
    title: String?,
    showLineNumbers: Boolean,
    startLine: Int,
    highlightedLines: Set<Int>,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val codeTheme = LocalCodeHighlightTheme.current ?: LocalCodeTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.codeBlockCornerRadius))
    ) {
        CodeBlock(
            code = text,
            language = language,
            title = title.orEmpty(),
            modifier = Modifier.fillMaxWidth(),
            theme = codeTheme,
            showLineNumbers = showLineNumbers,
            startLine = startLine,
            highlightedLines = highlightedLines,
        )
    }
}
