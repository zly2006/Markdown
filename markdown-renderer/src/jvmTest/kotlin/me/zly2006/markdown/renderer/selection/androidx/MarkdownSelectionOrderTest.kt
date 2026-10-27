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

package me.zly2006.markdown.renderer.selection.androidx

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownSelectionOrderTest {
    @Test
    fun sortsByDocumentOrderInsteadOfRegistrationOrder() {
        val reverseVisitedOrders = (199 downTo 0).map { listOf(it, 0) }

        assertEquals(
            (0..199).map { listOf(it, 0) },
            reverseVisitedOrders.sortedWith(::compareMarkdownDocumentOrder),
        )
    }

    @Test
    fun nestedGroupStaysBetweenParentSelectables() {
        val parentBeforeGroup = listOf(4, 0)
        val childInGroup = listOf(4, 1, 12, 0)
        val parentAfterGroup = listOf(4, 2)

        assertEquals(
            listOf(parentBeforeGroup, childInGroup, parentAfterGroup),
            listOf(parentAfterGroup, childInGroup, parentBeforeGroup)
                .sortedWith(::compareMarkdownDocumentOrder),
        )
    }
}
