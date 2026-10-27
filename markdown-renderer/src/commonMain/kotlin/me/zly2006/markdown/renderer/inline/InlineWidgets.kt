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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import com.hrm.codehigh.theme.CodeTheme
import com.hrm.latex.renderer.LatexRenderCache
import me.zly2006.markdown.parser.ast.Spoiler
import me.zly2006.markdown.renderer.MarkdownTheme
import me.zly2006.markdown.runtime.MarkdownDirectiveRegistry

@Composable
internal fun SpoilerContent(
    node: Spoiler,
    theme: MarkdownTheme,
    hostTextStyle: TextStyle,
    inlineContents: MutableMap<String, InlineContentEntry>,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    latexRenderCache: LatexRenderCache?,
    density: Density?,
    textMeasurer: TextMeasurer?,
    inlineCodeTheme: CodeTheme?,
) {
    var revealed by remember(node) { mutableStateOf(false) }
    val currentOnLinkClick = rememberUpdatedState(onLinkClick)
    val currentOnFootnoteClick = rememberUpdatedState(onFootnoteClick)
    val stableOnLinkClick: ((String) -> Unit)? = remember {
        { url: String ->
            currentOnLinkClick.value?.invoke(url)
        }
    }
    val stableOnFootnoteClick: ((String) -> Unit)? = remember {
        { label: String ->
            currentOnFootnoteClick.value?.invoke(label)
        }
    }
    val annotated = remember(
        node,
        theme,
        revealed,
        hostTextStyle,
        directiveRegistry,
        latexRenderCache,
        density,
        textMeasurer,
        inlineCodeTheme,
    ) {
        buildAnnotatedString {
            if (revealed) {
                withStyle(SpanStyle(background = theme.spoilerColor)) {
                    renderInlineChildren(
                        node.children,
                        theme,
                        hostTextStyle,
                        inlineContents,
                        directiveRegistry,
                        stableOnLinkClick,
                        stableOnFootnoteClick,
                        latexRenderCache,
                        density,
                        textMeasurer,
                        inlineCodeTheme,
                    )
                }
            } else {
                withStyle(
                    SpanStyle(
                        background = theme.spoilerColor,
                        color = theme.spoilerColor,
                    ),
                ) {
                    renderInlineChildren(
                        node.children,
                        theme,
                        hostTextStyle,
                        inlineContents,
                        directiveRegistry,
                        stableOnLinkClick,
                        stableOnFootnoteClick,
                        latexRenderCache,
                        density,
                        textMeasurer,
                        inlineCodeTheme,
                    )
                }
            }
        }
    }
    BasicText(
        text = annotated,
        modifier = Modifier.clickable { revealed = !revealed },
        style = theme.bodyStyle,
    )
}

@Composable
internal fun RubyTextContent(
    base: String,
    annotation: String,
    theme: MarkdownTheme,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(
            text = annotation,
            style = theme.bodyStyle.copy(
                fontSize = theme.bodyStyle.fontSize * 0.5f,
                lineHeight = theme.bodyStyle.fontSize * 0.6f,
            ),
        )
        BasicText(
            text = base,
            style = theme.bodyStyle.copy(
                lineHeight = theme.bodyStyle.fontSize * 1.2f,
            ),
        )
    }
}
