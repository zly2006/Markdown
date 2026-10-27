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
import me.zly2006.markdown.parser.ast.FrontMatter
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 前置元数据块开启器：`---` (YAML) 或 `+++` (TOML)。
 * 仅在文档第一行生效。
 *
 * 无状态设计：不依赖 SourceText，关闭标记由 [BlockParser] 的 addLineToTip 检测。
 * 若文档结束时仍未遇到关闭标记，[BlockParser.finalizeBlock] 会将其降级为 ThematicBreak + Paragraph。
 */
class FrontMatterStarter : BlockStarter {
    override val priority: Int = 10
    override val canInterruptParagraph: Boolean = false

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        if (lineIdx != 0) return null
        val rest = cursor.rest().trim()
        val format = when {
            rest == "---" -> "yaml"
            rest == "+++" -> "toml"
            else -> return null
        }

        val block = FrontMatter(format = format)
        block.lineRange = LineRange(lineIdx, lineIdx + 1)
        val ob = OpenBlock(block, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.starterTag = this::class.simpleName
        return ob
    }
}
