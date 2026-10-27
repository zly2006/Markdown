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

package me.zly2006.markdown.parser.block.starters

import me.zly2006.markdown.parser.LineRange
import me.zly2006.markdown.parser.ast.FencedCodeBlock
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.AttributeParser
import me.zly2006.markdown.parser.core.CharacterUtils
import me.zly2006.markdown.parser.core.HtmlEntities
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 围栏代码块开启器：``` 或 ~~~。
 */
internal class FencedCodeBlockStarter : BlockStarter {
    override val priority: Int = 310
    override val canInterruptParagraph: Boolean = true

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        val indent = cursor.advanceSpaces(3)
        if (cursor.isAtEnd) return null

        val c = cursor.peek()
        if (c != '`' && c != '~') return null

        var fenceLength = 0
        while (!cursor.isAtEnd && cursor.peek() == c) {
            cursor.advance()
            fenceLength++
        }
        if (fenceLength < 3) return null

        val info = cursor.rest().trim()
        if (c == '`' && info.contains('`')) return null

        cursor.advance(cursor.remaining)

        // parse {.class #id key=value} from info-string, leave the rest for language extraction
        val (attributes, infoWithoutAttrs) = AttributeParser.parse(info)
        val rawLang = infoWithoutAttrs.split(INFO_LANG_SPLIT_REGEX).firstOrNull()?.trim() ?: ""
        val language = HtmlEntities.replaceAll(resolveBackslashEscapes(rawLang))

        val block = FencedCodeBlock(
            info = info,
            language = language,
            fenceChar = c,
            fenceLength = fenceLength,
            fenceIndent = indent,
            attributes = attributes,
        )
        // parse extended code block attributes from the attribute pairs
        parseCodeBlockEnhancements(block, attributes.pairs)
        block.lineRange = LineRange(lineIdx, lineIdx + 1)

        val ob = OpenBlock(block, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.isFenced = true
        ob.fenceChar = c
        ob.fenceLength = fenceLength
        ob.fenceIndent = indent
        ob.starterTag = this::class.simpleName
        return ob
    }

    private fun resolveBackslashEscapes(s: String): String {
        val sb = StringBuilder(s.length)
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 1 < s.length && CharacterUtils.isAsciiPunctuation(s[i + 1])) {
                sb.append(s[i + 1])
                i += 2
            } else {
                sb.append(s[i])
                i++
            }
        }
        return sb.toString()
    }

    companion object {
        private val INFO_LANG_SPLIT_REGEX = Regex("\\s+")

        /**
         * parses extended code block attributes: hl_lines, linenums, startline.
         * these are extracted from the attribute pairs parsed by AttributeParser.
         */
        fun parseCodeBlockEnhancements(block: FencedCodeBlock, pairs: Map<String, String>) {
            val highlightSpec = pairs["hl_lines"] ?: pairs["highlight"]
            if (!highlightSpec.isNullOrBlank()) {
                block.highlightLines = parseHighlightLines(highlightSpec)
            }

            val lineNumbers = when {
                pairs["linenums"] != null -> pairs["linenums"]!!.equals("true", ignoreCase = true)
                pairs["linenos"] != null -> pairs["linenos"]!!.equals("true", ignoreCase = true)
                pairs["lineNumbers"] != null -> pairs["lineNumbers"]!!.equals("true", ignoreCase = true)
                block.attributes.classes.contains("line-numbers") -> true
                else -> true
            }
            block.showLineNumbers = lineNumbers

            pairs["startline"]?.toIntOrNull()?.let { startLine ->
                if (startLine > 0) {
                    block.startLineNumber = startLine
                }
            }
        }

        /**
         * parses highlight line specification like "1 3-5 8" into a list of IntRange.
         */
        fun parseHighlightLines(spec: String): List<IntRange> {
            val ranges = mutableListOf<IntRange>()
            for (part in spec.trim().split(Regex("[,\\s]+"))) {
                if (part.isEmpty()) continue
                val dashIdx = part.indexOf('-')
                if (dashIdx > 0) {
                    val start = part.substring(0, dashIdx).toIntOrNull() ?: continue
                    val end = part.substring(dashIdx + 1).toIntOrNull() ?: continue
                    if (start > 0 && end >= start) {
                        ranges.add(start..end)
                    }
                } else {
                    val line = part.toIntOrNull() ?: continue
                    if (line > 0) {
                        ranges.add(line..line)
                    }
                }
            }
            return ranges
        }
    }
}
