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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.hrm.codehigh.renderer.CodeBlock
import com.hrm.codehigh.theme.LocalCodeTheme
import me.zly2006.markdown.renderer.LocalCodeHighlightTheme
import me.zly2006.markdown.renderer.LocalIsStreaming
import me.zly2006.markdown.renderer.LocalMarkdownTheme

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
    text: String,
    language: String,
    title: String?,
    showLineNumbers: Boolean,
    startLine: Int,
    highlightedLines: Set<Int>,
    modifier: Modifier = Modifier,
) {
    CodeBlockText(
        text = text.ifEmpty { " " },
        language = language,
        title = title,
        showLineNumbers = showLineNumbers,
        startLine = startLine,
        highlightedLines = highlightedLines,
        modifier = modifier,
    )
}

/**
 * 缩进代码块渲染器
 */
@Composable
internal fun IndentedCodeBlockRenderer(
    text: String,
    modifier: Modifier = Modifier,
) {
    CodeBlockText(
        text = text.ifEmpty { " " },
        language = "",
        title = null,
        showLineNumbers = true,
        startLine = 1,
        highlightedLines = emptySet(),
        modifier = modifier,
    )
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
    val isStreaming = LocalIsStreaming.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.codeBlockCornerRadius)),
    ) {
        CodeBlock(
            code = text,
            language = language,
            title = title.orEmpty(),
            modifier = Modifier.fillMaxWidth(),
            isStreaming = isStreaming,
            theme = codeTheme,
            showLineNumbers = showLineNumbers,
            startLine = startLine,
            highlightedLines = highlightedLines,
        )
    }
}
