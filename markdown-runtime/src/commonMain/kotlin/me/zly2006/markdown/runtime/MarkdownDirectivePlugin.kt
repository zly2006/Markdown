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

/**
 * Markdown 扩展插件。
 *
 * 一个插件可以同时提供：
 * - 输入转换器：把外部特殊语法转换成官方 directive
 * - 块级 directive 渲染器：把 directive 渲染为原生 Compose 内容
 * - 行内 directive 渲染器：把行内 directive 渲染为原生 Compose 内容
 * - HTML fallback：为 HTML 导出提供自定义降级输出
 */
interface MarkdownDirectivePlugin {
    val id: String
    val priority: Int get() = 0
    val inputTransformers: List<MarkdownInputTransformer> get() = emptyList()
    val blockDirectiveRenderers: Map<String, MarkdownBlockDirectiveRenderer> get() = emptyMap()
    val inlineDirectiveRenderers: Map<String, MarkdownInlineDirectiveRenderer> get() = emptyMap()
    val htmlDirectiveFallbacks: Map<String, HtmlDirectiveFallback> get() = emptyMap()
    val htmlInlineDirectiveFallbacks: Map<String, HtmlInlineDirectiveFallback> get() = emptyMap()
}
