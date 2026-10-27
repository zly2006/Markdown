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

package me.zly2006.markdown.runtime

import androidx.compose.runtime.Composable
import me.zly2006.markdown.parser.ast.DirectiveBlock
import me.zly2006.markdown.parser.ast.DirectiveInline

/**
 * 外部块级 directive 原生渲染器。
 */
typealias MarkdownBlockDirectiveRenderer = @Composable (DirectiveBlockRenderScope) -> Unit

/**
 * 向外部插件暴露的 directive 渲染上下文。
 */
data class DirectiveBlockRenderScope(
    val tagName: String,
    val args: Map<String, String>,
    val node: DirectiveBlock,
    val content: (@Composable () -> Unit)? = null,
)

/**
 * 外部行内 directive 原生渲染器。
 */
typealias MarkdownInlineDirectiveRenderer = @Composable (DirectiveInlineRenderScope) -> Unit

/**
 * 向外部插件暴露的行内 directive 渲染上下文。
 */
data class DirectiveInlineRenderScope(
    val tagName: String,
    val args: Map<String, String>,
    val node: DirectiveInline,
    val alternateText: String,
)
