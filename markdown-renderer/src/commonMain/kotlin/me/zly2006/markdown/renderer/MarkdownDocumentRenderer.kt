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

package me.zly2006.markdown.renderer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hrm.codehigh.theme.CodeTheme
import com.hrm.latex.renderer.LocalLatexRenderCache
import com.hrm.latex.renderer.rememberLatexRenderCache
import me.zly2006.markdown.parser.ast.Document
import me.zly2006.markdown.runtime.MarkdownDirectiveRegistry

@Composable
internal fun MarkdownDocumentRenderer(
    document: Document,
    modifier: Modifier = Modifier,
    theme: MarkdownTheme = MarkdownTheme.auto(),
    codeTheme: CodeTheme? = null,
    config: MarkdownConfig = MarkdownConfig.Default,
    scrollState: ScrollState = rememberScrollState(),
    isStreaming: Boolean = false,
    enablePagination: Boolean = false,
    enableScroll: Boolean = true,
    enableSelection: Boolean = true,
    initialBlockCount: Int = 100,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    imageContent: MarkdownImageRenderer? = null,
    onLinkClick: ((String) -> Unit)? = null,
    directiveRegistry: MarkdownDirectiveRegistry = MarkdownDirectiveRegistry.Empty,
) {
    val renderMode = remember(enableSelection, enableScroll, isStreaming) {
        resolveMarkdownRenderMode(
            enableSelection = enableSelection,
            enableScroll = enableScroll,
            isStreaming = isStreaming,
        )
    }
    val lazyListState = rememberLazyListState()
    val inheritedLatexRenderCache = LocalLatexRenderCache.current
    val ownedLatexRenderCache = rememberLatexRenderCache()
    val latexRenderCache = inheritedLatexRenderCache ?: ownedLatexRenderCache
    val renderDocument = rememberRenderDocument(
        document = document,
        isStreaming = isStreaming,
    )
    val renderState = rememberMarkdownBlockRenderState(
        document = renderDocument,
        renderMode = renderMode,
        enablePagination = enablePagination,
        initialBlockCount = initialBlockCount,
        scrollState = scrollState,
        isStreaming = isStreaming,
        hasHeader = header != null,
    )
    val navigationHandlers = rememberMarkdownNavigationHandlers(
        renderMode = renderMode,
        enableScroll = enableScroll,
        scrollState = scrollState,
        lazyListState = lazyListState,
        renderState = renderState,
        onLinkClick = onLinkClick,
    )

    CompositionLocalProvider(LocalLatexRenderCache provides latexRenderCache) {
        ProvideMarkdownTheme(theme) {
            ProvideRendererContext(
                document = renderDocument,
                onLinkClick = onLinkClick,
                onFootnoteClick = navigationHandlers.onFootnoteClick,
                onFootnoteBackClick = navigationHandlers.onFootnoteBackClick,
                footnoteNavigationState = navigationHandlers.footnoteNavigationState,
                imageContent = imageContent,
                config = config,
                codeTheme = codeTheme,
                isStreaming = isStreaming,
                directiveRegistry = directiveRegistry,
            ) {
                MarkdownDocumentLayout(
                    renderMode = renderMode,
                    renderState = renderState,
                    modifier = modifier,
                    enableScroll = enableScroll,
                    scrollState = scrollState,
                    lazyListState = lazyListState,
                    header = header,
                    footer = footer,
                )
            }
        }
    }
}
