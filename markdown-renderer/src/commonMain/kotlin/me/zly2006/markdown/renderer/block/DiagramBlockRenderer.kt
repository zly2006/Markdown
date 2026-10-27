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

package me.zly2006.markdown.renderer.block

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import me.zly2006.markdown.parser.ast.DiagramBlock
import me.zly2006.markdown.renderer.LocalMarkdownTheme
import me.zly2006.markdown.renderer.diagram.DiagramFallback
import me.zly2006.markdown.renderer.diagram.GraphvizDiagram
import me.zly2006.markdown.renderer.diagram.MermaidFlowchartDiagram
import me.zly2006.markdown.renderer.diagram.MermaidSequenceDiagram
import me.zly2006.markdown.renderer.diagram.PlantUMLSequenceDiagram

/**
 * 图表块渲染器（Mermaid / PlantUML 等）。
 *
 * 根据图表类型分发到对应的渲染引擎：
 * - Mermaid flowchart/graph → Canvas 绘制流程图
 * - PlantUML sequence → Canvas 绘制时序图
 * - 其他未支持类型 → 代码展示 fallback
 */
@Composable
internal fun DiagramBlockRenderer(
    node: DiagramBlock,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownTheme.current
    val code = node.literal.trimEnd('\n')
    val diagramType = node.diagramType.lowercase()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.codeBlockCornerRadius))
            .background(Color(0xFFF8FAFC))
            .padding(theme.codeBlockPadding),
    ) {
        when {
            diagramType == "mermaid" -> {
                // 判断是 flowchart / graph / sequence 等
                val firstLine = code
                    .lines()
                    .firstOrNull()
                    ?.trim()
                    ?.lowercase() ?: ""
                when {
                    firstLine.startsWith("flowchart") || firstLine.startsWith("graph") -> {
                        MermaidFlowchartDiagram(code)
                    }
                    firstLine.startsWith("sequencediagram") || firstLine.startsWith("sequence") -> {
                        MermaidSequenceDiagram(code)
                    }
                    else -> {
                        // 尝试解析为 flowchart，失败则 fallback
                        MermaidFlowchartDiagram(code)
                    }
                }
            }
            diagramType == "plantuml" -> {
                PlantUMLSequenceDiagram(code)
            }
            diagramType in setOf("dot", "graphviz") -> {
                GraphvizDiagram(code)
            }
            else -> {
                val typeName = node.diagramType.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
                DiagramFallback(code, typeName)
            }
        }
    }
}
