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

import me.zly2006.markdown.parser.log.HLog

private const val TAG = "MarkdownDirectiveRegistry"

class MarkdownDirectiveRegistry(
    directivePlugins: List<MarkdownDirectivePlugin>,
) {
    val directivePlugins: List<MarkdownDirectivePlugin> = directivePlugins
        .sortedWith(compareBy<MarkdownDirectivePlugin> { it.priority }.thenBy { it.id })
        .toList()

    private val blockRenderers = LinkedHashMap<String, MarkdownBlockDirectiveRenderer>()
    private val inlineRenderers = LinkedHashMap<String, MarkdownInlineDirectiveRenderer>()
    private val htmlFallbacks = LinkedHashMap<String, HtmlDirectiveFallback>()
    private val htmlInlineFallbacks = LinkedHashMap<String, HtmlInlineDirectiveFallback>()
    private val transformers = mutableListOf<MarkdownInputTransformer>()

    /**
     * 当前实现只在“没有 transformer”时保留 streaming fast path。
     */
    val supportsStreamingFastPath: Boolean
        get() = transformers.isEmpty()

    val hasTransformers: Boolean
        get() = transformers.isNotEmpty()

    init {
        for (plugin in this.directivePlugins) {
            transformers += plugin.inputTransformers
            for ((tagName, renderer) in plugin.blockDirectiveRenderers) {
                if (blockRenderers.containsKey(tagName)) {
                    HLog.w(TAG) { "block directive renderer overridden: tag=$tagName, plugin=${plugin.id}" }
                }
                blockRenderers[tagName] = renderer
            }
            for ((tagName, renderer) in plugin.inlineDirectiveRenderers) {
                if (inlineRenderers.containsKey(tagName)) {
                    HLog.w(TAG) { "inline directive renderer overridden: tag=$tagName, plugin=${plugin.id}" }
                }
                inlineRenderers[tagName] = renderer
            }
            for ((tagName, fallback) in plugin.htmlDirectiveFallbacks) {
                if (htmlFallbacks.containsKey(tagName)) {
                    HLog.w(TAG) { "html directive fallback overridden: tag=$tagName, plugin=${plugin.id}" }
                }
                htmlFallbacks[tagName] = fallback
            }
            for ((tagName, fallback) in plugin.htmlInlineDirectiveFallbacks) {
                if (htmlInlineFallbacks.containsKey(tagName)) {
                    HLog.w(TAG) { "html inline directive fallback overridden: tag=$tagName, plugin=${plugin.id}" }
                }
                htmlInlineFallbacks[tagName] = fallback
            }
        }
    }

    fun inputTransformers(): List<MarkdownInputTransformer> = transformers.toList()

    fun findBlockDirectiveRenderer(tagName: String): MarkdownBlockDirectiveRenderer? = blockRenderers[tagName]

    fun findInlineDirectiveRenderer(tagName: String): MarkdownInlineDirectiveRenderer? = inlineRenderers[tagName]

    fun findHtmlDirectiveFallback(tagName: String): HtmlDirectiveFallback? = htmlFallbacks[tagName]

    fun findHtmlInlineDirectiveFallback(tagName: String): HtmlInlineDirectiveFallback? = htmlInlineFallbacks[tagName]

    companion object {
        val Empty = MarkdownDirectiveRegistry(emptyList())
    }
}
