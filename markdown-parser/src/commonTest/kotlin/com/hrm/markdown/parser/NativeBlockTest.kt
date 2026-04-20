package com.hrm.markdown.parser

import com.hrm.markdown.parser.ast.Document
import com.hrm.markdown.parser.ast.NativeBlock
import com.hrm.markdown.parser.html.HtmlRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NativeBlockTest {
    @Test
    fun native_block_should_be_ignored_by_html_renderer() {
        val document = Document().apply {
            appendChild(NativeBlock { })
        }

        assertEquals("", HtmlRenderer().render(document))
    }

    @Test
    fun native_block_should_allocate_unique_stable_keys() {
        val first = NativeBlock { }
        val second = NativeBlock { }

        assertNotEquals(first.stableKey, second.stableKey)
    }
}
