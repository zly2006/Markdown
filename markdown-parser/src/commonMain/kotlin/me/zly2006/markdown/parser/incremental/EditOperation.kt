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

/**
 * 编辑操作定义。
 *
 * 描述对源文本的一次编辑变更，用于增量解析引擎计算脏区域。
 */
sealed class EditOperation {
    /**
     * 在指定偏移量处插入文本。
     */
    data class Insert(
        val offset: Int,
        val text: String,
    ) : EditOperation()

    /**
     * 从指定偏移量开始删除指定长度的文本。
     */
    data class Delete(
        val offset: Int,
        val length: Int,
    ) : EditOperation()

    /**
     * 替换：从指定偏移量开始，删除指定长度的文本并插入新文本。
     */
    data class Replace(
        val offset: Int,
        val length: Int,
        val newText: String,
    ) : EditOperation()

    /**
     * 追加：在文本末尾追加（流式场景的特化形式）。
     */
    data class Append(
        val text: String,
    ) : EditOperation()
}
