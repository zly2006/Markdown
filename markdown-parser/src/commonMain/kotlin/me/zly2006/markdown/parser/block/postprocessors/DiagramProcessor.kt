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

import me.zly2006.markdown.parser.ast.ContainerNode
import me.zly2006.markdown.parser.ast.DiagramBlock
import me.zly2006.markdown.parser.ast.Document
import me.zly2006.markdown.parser.ast.FencedCodeBlock
import me.zly2006.markdown.parser.ast.Node

/**
 * 后处理：将 info string 为 mermaid/plantuml 等的 FencedCodeBlock 转换为 DiagramBlock。
 */
class DiagramProcessor : PostProcessor {
    override val priority: Int = 400

    override fun process(document: Document) {
        processRecursive(document)
    }

    private fun processRecursive(node: Node) {
        if (node is ContainerNode) {
            val children = node.children.toList()
            for (child in children) {
                if (child is FencedCodeBlock && child.language.lowercase() in DIAGRAM_LANGUAGES) {
                    val diagram = DiagramBlock(
                        diagramType = child.language.lowercase(),
                        literal = child.literal,
                    )
                    diagram.lineRange = child.lineRange
                    diagram.sourceRange = child.sourceRange
                    diagram.contentHash = child.contentHash
                    node.replaceChild(child, diagram)
                } else {
                    processRecursive(child)
                }
            }
        }
    }

    companion object {
        private val DIAGRAM_LANGUAGES = setOf(
            "mermaid",
            "plantuml",
            "dot",
            "graphviz",
            "ditaa",
            "flowchart",
            "sequence",
            "gantt",
            "pie",
            "mindmap",
        )
    }
}
