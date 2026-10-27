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

package me.zly2006.markdown.parser.incremental

import me.zly2006.markdown.parser.LineRange
import me.zly2006.markdown.parser.ast.Node
import me.zly2006.markdown.parser.core.SourceText

/**
 * 节点复用策略。
 *
 * 在增量解析中，判断旧 AST 的哪些子树可以直接复用，
 * 避免不必要的重新解析和重新渲染。
 *
 * 复用条件：
 * 1. 节点的行范围完全在脏区域之外
 * 2. 节点对应的源文本内容哈希与旧节点一致
 */
class NodeReuser {
    /**
     * 从旧文档的顶层子节点中，找出脏区域之前可复用的节点。
     *
     * @param oldChildren 旧文档的子节点列表
     * @param dirtyRange 脏区域行范围（在新源文本坐标系中）
     * @param linesDelta 编辑引起的行数偏移量
     * @return 可复用的前缀节点数量
     */
    fun findReusablePrefixCount(
        oldChildren: List<Node>,
        dirtyRange: LineRange,
    ): Int {
        var count = 0
        for (child in oldChildren) {
            if (child.lineRange.endLine <= dirtyRange.startLine) {
                count++
            } else {
                break
            }
        }
        return count
    }

    /**
     * 从旧文档的顶层子节点中，找出脏区域之后可复用的节点。
     * 这些节点的行范围需要进行偏移调整。
     *
     * @param oldChildren 旧文档的子节点列表
     * @param dirtyRange 脏区域行范围（在旧源文本坐标系中）
     * @param linesDelta 行数偏移量（新行数 - 旧行数在脏区域中的差值）
     * @param newSource 新源文本
     * @return 可复用的后缀节点列表（已调整行范围和哈希）
     */
    fun findReusableSuffix(
        oldChildren: List<Node>,
        dirtyRangeOld: LineRange,
        linesDelta: Int,
        newSource: SourceText,
    ): List<Node> {
        val result = mutableListOf<Node>()
        for (child in oldChildren) {
            if (child.lineRange.startLine >= dirtyRangeOld.endLine) {
                // 调整行范围
                val newLineRange = child.lineRange.shift(linesDelta)
                // 验证哈希
                if (newLineRange.startLine >= 0 &&
                    newLineRange.endLine <= newSource.lineCount
                ) {
                    val newHash = newSource.contentHash(newLineRange)
                    if (newHash == child.contentHash) {
                        child.lineRange = newLineRange
                        result.add(child)
                    }
                }
            }
        }
        return result
    }
}
