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
import me.zly2006.markdown.parser.ast.DefinitionDescription
import me.zly2006.markdown.parser.ast.DefinitionList
import me.zly2006.markdown.parser.ast.FootnoteDefinition
import me.zly2006.markdown.parser.ast.IndentedCodeBlock
import me.zly2006.markdown.parser.block.OpenBlock
import me.zly2006.markdown.parser.core.LineCursor

/**
 * 缩进代码块开启器：4 个空格或 1 个制表符缩进。
 * 优先级最低，不能中断段落。
 *
 * 在以下容器内部不触发，避免与扩展语法冲突：
 * - [DefinitionList] / [DefinitionDescription]：缩进内容属于定义描述的续行
 * - [FootnoteDefinition]：缩进内容属于脚注的续行
 */
internal class IndentedCodeBlockStarter : BlockStarter {
    override val priority: Int = 600
    override val canInterruptParagraph: Boolean = false

    override fun tryStart(cursor: LineCursor, lineIdx: Int, tip: OpenBlock): OpenBlock? {
        if (tip.paragraphContent != null) return null
        // 在定义列表或脚注内部，缩进是续行内容而非代码块
        if (tip.node is DefinitionList || tip.node is DefinitionDescription || tip.node is FootnoteDefinition) {
            return null
        }
        val indent = cursor.advanceSpaces(4)
        if (indent < 4) return null

        val block = IndentedCodeBlock()
        block.lineRange = LineRange(lineIdx, lineIdx + 1)

        val ob = OpenBlock(block, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.starterTag = this::class.simpleName
        return ob
    }
}
