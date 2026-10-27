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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.hrm.codehigh.theme.CodeTheme
import com.hrm.codehigh.theme.LocalCodeTheme
import com.hrm.latex.renderer.LatexRenderCache
import com.hrm.latex.renderer.LocalLatexRenderCache
import com.hrm.latex.renderer.measure.LatexDimensions
import me.zly2006.markdown.parser.ast.Abbreviation
import me.zly2006.markdown.parser.ast.Autolink
import me.zly2006.markdown.parser.ast.CitationReference
import me.zly2006.markdown.parser.ast.ContainerNode
import me.zly2006.markdown.parser.ast.DirectiveInline
import me.zly2006.markdown.parser.ast.Emoji
import me.zly2006.markdown.parser.ast.Emphasis
import me.zly2006.markdown.parser.ast.EscapedChar
import me.zly2006.markdown.parser.ast.FootnoteReference
import me.zly2006.markdown.parser.ast.HardLineBreak
import me.zly2006.markdown.parser.ast.Highlight
import me.zly2006.markdown.parser.ast.HtmlEntity
import me.zly2006.markdown.parser.ast.Image
import me.zly2006.markdown.parser.ast.InlineCode
import me.zly2006.markdown.parser.ast.InlineHtml
import me.zly2006.markdown.parser.ast.InlineMath
import me.zly2006.markdown.parser.ast.InsertedText
import me.zly2006.markdown.parser.ast.KeyboardInput
import me.zly2006.markdown.parser.ast.Link
import me.zly2006.markdown.parser.ast.Node
import me.zly2006.markdown.parser.ast.RubyText
import me.zly2006.markdown.parser.ast.SegmentHighlight
import me.zly2006.markdown.parser.ast.SoftLineBreak
import me.zly2006.markdown.parser.ast.Spoiler
import me.zly2006.markdown.parser.ast.Strikethrough
import me.zly2006.markdown.parser.ast.StrongEmphasis
import me.zly2006.markdown.parser.ast.StyledText
import me.zly2006.markdown.parser.ast.Subscript
import me.zly2006.markdown.parser.ast.Superscript
import me.zly2006.markdown.parser.ast.Text
import me.zly2006.markdown.parser.ast.WikiLink
import me.zly2006.markdown.renderer.LocalCodeHighlightTheme
import me.zly2006.markdown.renderer.LocalFootnoteNavigationState
import me.zly2006.markdown.renderer.LocalMarkdownDirectiveRegistry
import me.zly2006.markdown.renderer.LocalMarkdownTheme
import me.zly2006.markdown.renderer.LocalOnFootnoteClick
import me.zly2006.markdown.renderer.MarkdownTheme
import me.zly2006.markdown.runtime.MarkdownDirectiveRegistry

@Composable
internal fun rememberInlineContent(
    parent: ContainerNode,
    onLinkClick: ((String) -> Unit)? = null,
    hostTextStyle: TextStyle = LocalMarkdownTheme.current.bodyStyle,
): InlineContentResult {
    val theme = LocalMarkdownTheme.current
    val directiveRegistry = LocalMarkdownDirectiveRegistry.current
    val onFootnoteClick = LocalOnFootnoteClick.current
    val latexRenderCache = LocalLatexRenderCache.current
    val density = LocalDensity.current
    val latexDimensionsKey = inlineMathDimensionsKey(parent.children, latexRenderCache, theme, density)
    val textMeasurer = rememberTextMeasurer()
    val inlineCodeTheme = LocalCodeHighlightTheme.current ?: LocalCodeTheme.current
    val inlineRevision = remember(parent) {
        {
            var acc = parent.contentHash
            acc = acc * 31 + parent.lineRange.endLine.toLong()
            acc = acc * 31 + parent.childCount().toLong()
            acc
        }
    }
    return remember(
        parent,
        inlineRevision(),
        theme,
        directiveRegistry,
        onLinkClick,
        onFootnoteClick,
        hostTextStyle,
        latexRenderCache,
        latexDimensionsKey,
        density,
        textMeasurer,
        inlineCodeTheme,
    ) {
        val inlineContents = mutableMapOf<String, InlineContentEntry>()
        val annotated = buildAnnotatedString {
            renderInlineChildren(
                parent.children,
                theme,
                hostTextStyle,
                inlineContents,
                directiveRegistry,
                onLinkClick,
                onFootnoteClick,
                latexRenderCache,
                density,
                textMeasurer,
                inlineCodeTheme,
            )
        }
        InlineContentResult(
            annotated = annotated,
            inlineContents = inlineContents,
        )
    }
}

internal fun buildInlineAnnotatedString(
    nodes: List<Node>,
    theme: MarkdownTheme,
    hostTextStyle: TextStyle,
    inlineContents: MutableMap<String, InlineContentEntry>,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)? = null,
    onFootnoteClick: ((String) -> Unit)? = null,
    latexRenderCache: LatexRenderCache? = null,
    density: Density? = null,
    textMeasurer: TextMeasurer? = null,
    codeTheme: CodeTheme? = null,
): AnnotatedString = buildAnnotatedString {
    renderInlineChildren(
        nodes,
        theme,
        hostTextStyle,
        inlineContents,
        directiveRegistry,
        onLinkClick,
        onFootnoteClick,
        latexRenderCache,
        density,
        textMeasurer,
        codeTheme,
    )
}

internal fun AnnotatedString.Builder.renderInlineChildren(
    nodes: List<Node>,
    theme: MarkdownTheme,
    hostTextStyle: TextStyle,
    inlineContents: MutableMap<String, InlineContentEntry>,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    latexRenderCache: LatexRenderCache? = null,
    density: Density? = null,
    textMeasurer: TextMeasurer? = null,
    inlineCodeTheme: CodeTheme? = null,
) {
    for (node in nodes) {
        renderInlineNode(
            node,
            theme,
            hostTextStyle,
            inlineContents,
            directiveRegistry,
            onLinkClick,
            onFootnoteClick,
            latexRenderCache,
            density,
            textMeasurer,
            inlineCodeTheme,
        )
    }
}

internal fun AnnotatedString.Builder.renderInlineNode(
    node: Node,
    theme: MarkdownTheme,
    hostTextStyle: TextStyle,
    inlineContents: MutableMap<String, InlineContentEntry>,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    latexRenderCache: LatexRenderCache? = null,
    density: Density? = null,
    textMeasurer: TextMeasurer? = null,
    inlineCodeTheme: CodeTheme? = null,
) {
    when (node) {
        is Text -> append(node.literal)

        is SoftLineBreak -> append(" ")

        is HardLineBreak -> append("\n")

        is Emphasis -> {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is StrongEmphasis -> {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is Strikethrough -> {
            withStyle(theme.strikethroughStyle) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is InlineCode -> {
            renderInlineCodeNode(
                node,
                theme,
                inlineContents,
                density,
                textMeasurer,
                inlineCodeTheme,
            )
        }

        is Link -> {
            pushStringAnnotation(MARKDOWN_LINK_ANNOTATION_TAG, node.destination)
            withStyle(
                SpanStyle(
                    color = theme.linkColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
            pop()
        }

        is Image -> {
            renderImageNode(node, inlineContents)
        }

        is Autolink -> {
            pushStringAnnotation(MARKDOWN_LINK_ANNOTATION_TAG, node.destination)
            withStyle(
                SpanStyle(
                    color = theme.linkColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(node.destination)
            }
            pop()
        }

        is InlineHtml -> {
            withStyle(
                SpanStyle(
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                ),
            ) {
                append(node.literal)
            }
        }

        is HtmlEntity -> {
            append(node.resolved.ifEmpty { node.literal })
        }

        is EscapedChar -> {
            append(node.literal)
        }

        is FootnoteReference -> {
            val referenceText = "[${node.index}]"
            if (density != null && textMeasurer != null) {
                val referenceStyle = hostTextStyle.copy(
                    color = theme.linkColor,
                    fontSize = theme.footnoteStyle.fontSize,
                )
                val size = textMeasurer.measure(referenceText, style = referenceStyle).size
                val id = "footnote_ref_${node.hashCode()}"
                appendInlinePlaceholder(id)
                val inlineTextContent = InlineTextContent(
                    placeholder = Placeholder(
                        // PR #15 在 Android AVD 上会因像素到 sp 的舍入误差裁掉脚注末尾；仅补 1px。
                        // https://github.com/huarangmeng/Markdown/pull/15#issuecomment-4251156556
                        width = with(density) { (size.width + 1).toSp() },
                        height = hostTextStyle.lineHeight,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextTop,
                    ),
                ) {
                    FootnoteReferenceContent(
                        label = node.label,
                        text = referenceText,
                        hostTextStyle = hostTextStyle,
                        theme = theme,
                        onFootnoteClick = onFootnoteClick,
                    )
                }
                inlineContents[id] = InlineContentEntry(
                    alternateText = referenceText,
                    inlineTextContent = inlineTextContent,
                )
            } else {
                val linkAnnotation = LinkAnnotation.Clickable(
                    tag = "footnote",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = theme.linkColor,
                            fontSize = theme.footnoteStyle.fontSize,
                            baselineShift = BaselineShift.Superscript,
                        ),
                    ),
                    linkInteractionListener = {
                        onFootnoteClick?.invoke(node.label)
                    },
                )
                withLink(linkAnnotation) {
                    append(referenceText)
                }
            }
        }

        is InlineMath -> {
            renderInlineMathNode(
                node,
                theme,
                hostTextStyle,
                inlineContents,
                latexRenderCache,
                density,
                textMeasurer,
            )
        }

        is Highlight -> {
            withStyle(SpanStyle(background = theme.highlightColor)) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is SegmentHighlight -> {
            pushStringAnnotation(
                tag = SEGMENT_HIGHLIGHT_ANNOTATION_TAG,
                annotation = node.interactionKey,
            )
            renderInlineChildren(
                node.children,
                theme,
                hostTextStyle,
                inlineContents,
                directiveRegistry,
                onLinkClick,
                onFootnoteClick,
                latexRenderCache,
                density,
                textMeasurer,
                inlineCodeTheme,
            )
            pop()
        }

        is Superscript -> {
            withStyle(
                theme.superscriptStyle.merge(
                    SpanStyle(baselineShift = BaselineShift.Superscript),
                ),
            ) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is Subscript -> {
            withStyle(
                theme.subscriptStyle.merge(
                    SpanStyle(baselineShift = BaselineShift.Subscript),
                ),
            ) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is InsertedText -> {
            withStyle(theme.insertedTextStyle) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is Emoji -> {
            // 优先显示 unicode（已映射），否则显示 literal
            append(node.unicode ?: node.literal.ifEmpty { ":${node.shortcode}:" })
        }

        is StyledText -> {
            // 从属性中提取样式信息
            val styleStr = node.style
            val spanStyle = if (styleStr != null) {
                parseCssStyleToSpanStyle(styleStr)
            } else {
                // 根据 CSS class 推断样式
                inferStyleFromClasses(node.cssClasses, theme)
            }
            if (spanStyle != null) {
                withStyle(spanStyle) {
                    renderInlineChildren(
                        node.children,
                        theme,
                        hostTextStyle,
                        inlineContents,
                        directiveRegistry,
                        onLinkClick,
                        onFootnoteClick,
                        latexRenderCache,
                        density,
                        textMeasurer,
                        inlineCodeTheme,
                    )
                }
            } else {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }

        is Abbreviation -> {
            // embed fullText as annotation so consumers can show tooltip on hover/click
            if (node.fullText.isNotEmpty()) {
                pushStringAnnotation(tag = "abbreviation", annotation = node.fullText)
                withStyle(theme.abbreviationStyle) {
                    append(node.abbreviation)
                }
                pop()
            } else {
                withStyle(theme.abbreviationStyle) {
                    append(node.abbreviation)
                }
            }
        }

        is KeyboardInput -> {
            // 渲染键盘按键：等宽字体 + 背景
            withStyle(theme.kbdStyle) {
                append(node.literal)
            }
        }

        is CitationReference -> {
            val linkAnnotation = LinkAnnotation.Clickable(
                tag = "citation",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = theme.linkColor,
                        fontSize = theme.footnoteStyle.fontSize,
                        baselineShift = BaselineShift.Superscript,
                    ),
                ),
                linkInteractionListener = {
                    // 引用点击暂不处理，可扩展
                },
            )
            withLink(linkAnnotation) {
                append("[${node.key}]")
            }
        }

        is Spoiler -> {
            renderSpoilerNode(
                node,
                theme,
                hostTextStyle,
                inlineContents,
                directiveRegistry,
                onLinkClick,
                onFootnoteClick,
                latexRenderCache,
                density,
                textMeasurer,
                inlineCodeTheme,
            )
        }

        is DirectiveInline -> {
            renderDirectiveInlineNode(node, theme, inlineContents, directiveRegistry)
        }

        is WikiLink -> {
            val linkAnnotation = LinkAnnotation.Clickable(
                tag = "wikilink",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = theme.linkColor,
                        textDecoration = TextDecoration.Underline,
                    ),
                ),
                linkInteractionListener = {
                    onLinkClick?.invoke(node.target)
                },
            )
            withLink(linkAnnotation) {
                append(node.label ?: node.target)
            }
        }

        is RubyText -> {
            renderRubyTextNode(node, theme, inlineContents)
        }

        else -> {
            if (node is ContainerNode) {
                renderInlineChildren(
                    node.children,
                    theme,
                    hostTextStyle,
                    inlineContents,
                    directiveRegistry,
                    onLinkClick,
                    onFootnoteClick,
                    latexRenderCache,
                    density,
                    textMeasurer,
                    inlineCodeTheme,
                )
            }
        }
    }
}

internal fun inlineMathDimensionsKey(
    nodes: List<Node>,
    cache: LatexRenderCache?,
    theme: MarkdownTheme,
    density: Density,
): List<LatexDimensions?> {
    if (cache == null) return emptyList()
    val config = inlineMathLatexConfig(theme)
    val result = mutableListOf<LatexDimensions?>()

    fun visit(node: Node) {
        when (node) {
            is InlineMath -> {
                result += cache.observeDimensions(node.literal, config, density).value
            }
            is ContainerNode -> node.children.forEach(::visit)
            else -> Unit
        }
    }
    nodes.forEach(::visit)
    return result
}

@Composable
private fun FootnoteReferenceContent(
    label: String,
    text: String,
    hostTextStyle: TextStyle,
    theme: MarkdownTheme,
    onFootnoteClick: ((String) -> Unit)?,
) {
    val footnoteNavigationState = LocalFootnoteNavigationState.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    DisposableEffect(footnoteNavigationState, label, bringIntoViewRequester) {
        footnoteNavigationState?.registerReference(label, bringIntoViewRequester)
        onDispose {
            footnoteNavigationState?.unregisterReference(label, bringIntoViewRequester)
        }
    }

    BasicText(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = theme.linkColor,
                    fontSize = theme.footnoteStyle.fontSize,
                    baselineShift = BaselineShift.Superscript,
                ),
            ) {
                append(text)
            }
        },
        modifier = Modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .clickable { onFootnoteClick?.invoke(label) },
        style = hostTextStyle,
    )
}
