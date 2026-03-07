package com.hrm.markdown.parser.block.starters

import com.hrm.markdown.parser.LineRange
import com.hrm.markdown.parser.ast.FrontMatter
import com.hrm.markdown.parser.block.OpenBlock
import com.hrm.markdown.parser.core.LineCursor
import com.hrm.markdown.parser.core.SourceText

/**
 * 前置元数据块开启器：`---` (YAML) 或 `+++` (TOML)。
 * 仅在文档第一行生效。
 */
class FrontMatterStarter(
    private val source: SourceText
) : BlockStarter {
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

        val closingMarker = if (format == "yaml") "---" else "+++"
        var foundClosing = false
        for (i in lineIdx + 1 until source.lineCount) {
            if (source.lineContent(i).trim() == closingMarker) {
                foundClosing = true
                break
            }
        }
        if (!foundClosing) return null

        val block = FrontMatter(format = format)
        block.lineRange = LineRange(lineIdx, lineIdx + 1)
        val ob = OpenBlock(block, contentStartLine = lineIdx, lastLineIndex = lineIdx)
        ob.starterTag = this::class.simpleName
        return ob
    }
}
