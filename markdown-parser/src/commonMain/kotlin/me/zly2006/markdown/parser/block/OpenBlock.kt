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

package me.zly2006.markdown.parser.block

import me.zly2006.markdown.parser.ast.Node

/**
 * 表示解析过程中一个已打开（尚未关闭）的块。
 *
 * 从 BlockParser 中提取为包级类，供 BlockStarters、BlockStarter 等共享。
 */
class OpenBlock(
    val node: Node,
    var lastLineIndex: Int = 0,
    var contentLines: MutableList<String> = mutableListOf(),
    var contentStartLine: Int = 0,
) {
    var paragraphContent: StringBuilder? = null
    var isFenced: Boolean = false
    var fenceChar: Char = ' '
    var fenceLength: Int = 0
    var fenceIndent: Int = 0
    var htmlType: Int = 0
    var blankLineCount: Int = 0
    var blankLineAfterContent: Boolean = false

    /** 列表项元数据（仅 ListItem 类型使用） */
    var listItemMeta: ListItemMeta? = null

    /** 创建此块的开启器标签（用于注册制匹配） */
    var starterTag: String? = null

    /** 当前行内自包含块闭合标记之后剩余的文本。 */
    var trailingContent: String? = null
}
