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

package me.zly2006.markdown.parser.block.postprocessors

import me.zly2006.markdown.parser.ast.BibEntry
import me.zly2006.markdown.parser.ast.BibliographyDefinition
import me.zly2006.markdown.parser.ast.BlankLine
import me.zly2006.markdown.parser.ast.ContainerNode
import me.zly2006.markdown.parser.ast.Document
import me.zly2006.markdown.parser.ast.FootnoteDefinition
import me.zly2006.markdown.parser.ast.HardLineBreak
import me.zly2006.markdown.parser.ast.LeafNode
import me.zly2006.markdown.parser.ast.Node
import me.zly2006.markdown.parser.ast.Paragraph
import me.zly2006.markdown.parser.ast.SoftLineBreak
import me.zly2006.markdown.parser.ast.Text

/**
 * 参考文献后处理器。
 *
 * 在 AST 构建完成后：
 * 1. 扫描所有 `FootnoteDefinition`，查找 label 为 "bibliography" 的定义。
 * 2. 将其转换为 `BibliographyDefinition` 节点，解析其内容为 BibEntry 条目。
 * 3. 将文献条目存储到 Document 上，供行内 `CitationReference` 查找。
 */
internal class BibliographyProcessor : PostProcessor {
    override val priority: Int = 180

    override fun process(document: Document) {
        val bibDefs = mutableListOf<FootnoteDefinition>()
        collectBibliographyDefs(document, bibDefs)

        for (footnote in bibDefs) {
            val parent = footnote.parent as? ContainerNode ?: continue
            val nodes = bibliographyNodes(parent, footnote)
            val bibDef = BibliographyDefinition()
            bibDef.lineRange = footnote.lineRange
            bibDef.sourceRange = footnote.sourceRange

            // 解析脚注内容中的文献条目。
            // bibliography 在当前语法中允许紧随脚注之后的连续段落继续追加条目。
            // 格式：key: Author, "Title", Year
            val entries = parseBibEntries(nodes)
            for (entry in entries) {
                bibDef.entries[entry.key] = entry
            }

            // 替换原脚注节点
            parent.replaceChild(footnote, bibDef)
            removeContinuationParagraphs(parent, nodes)

            // 从 footnoteDefinitions 中移除
            document.footnoteDefinitions.remove("bibliography")
        }
    }

    private fun collectBibliographyDefs(node: Node, result: MutableList<FootnoteDefinition>) {
        if (node is FootnoteDefinition && node.label.equals("bibliography", ignoreCase = true)) {
            result.add(node)
        }
        if (node is ContainerNode) {
            for (child in node.children.toList()) {
                collectBibliographyDefs(child, result)
            }
        }
    }

    private fun parseBibEntries(nodes: List<Node>): List<BibEntry> {
        val entries = mutableListOf<BibEntry>()

        // 收集脚注子节点和紧随其后的连续段落文本。
        for (child in nodes) {
            if (child is BlankLine) continue
            val text = extractPlainText(child)
            // 逐行解析 key: content 格式
            for (line in text.lines()) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                val colonIdx = trimmed.indexOf(':')
                if (colonIdx > 0) {
                    val key = trimmed.substring(0, colonIdx).trim()
                    val content = trimmed.substring(colonIdx + 1).trim()
                    if (key.isNotEmpty() && content.isNotEmpty()) {
                        entries.add(BibEntry(key = key, content = content))
                    }
                }
            }
        }

        return entries
    }

    private fun removeContinuationParagraphs(parent: ContainerNode, nodes: List<Node>) {
        nodes
            .drop(1)
            .filterIsInstance<Paragraph>()
            .forEach { parent.removeChild(it) }
    }

    private fun bibliographyNodes(parent: ContainerNode, footnote: FootnoteDefinition): List<Node> {
        val siblings = parent.children
        val footnoteIndex = siblings.indexOf(footnote)
        if (footnoteIndex == -1) return listOf(footnote)

        val nodes = mutableListOf<Node>(footnote)
        var previousEndLine = footnote.lineRange.endLine
        for (i in footnoteIndex + 1 until siblings.size) {
            val sibling = siblings[i]
            if (sibling !is Paragraph) break
            if (sibling.lineRange.startLine > previousEndLine + 1) break
            nodes += sibling
            previousEndLine = sibling.lineRange.endLine
        }
        return nodes
    }

    private fun extractPlainText(node: Node): String = when (node) {
        is Text -> node.literal
        is SoftLineBreak -> "\n"
        is HardLineBreak -> "\n"
        is ContainerNode -> node.children.joinToString("") { extractPlainText(it) }
        is LeafNode -> node.literal
    }
}
