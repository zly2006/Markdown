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

package me.zly2006.markdown.renderer

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf

internal sealed interface FootnoteReturnPosition {
    data class Scroll(
        val value: Int,
    ) : FootnoteReturnPosition

    data class LazyList(
        val index: Int,
        val offset: Int,
    ) : FootnoteReturnPosition
}

@Stable
internal class FootnoteNavigationState {
    private val definitionRequesters = mutableMapOf<String, BringIntoViewRequester>()
    private val referenceRequesters = mutableMapOf<String, MutableList<BringIntoViewRequester>>()
    private val returnPositions = mutableStateMapOf<String, FootnoteReturnPosition>()
    private val requestedDefinitions = mutableStateMapOf<String, Unit>()
    private val requestedReferences = mutableStateMapOf<String, Unit>()

    fun registerDefinition(label: String, requester: BringIntoViewRequester) {
        definitionRequesters[label] = requester
    }

    fun unregisterDefinition(label: String, requester: BringIntoViewRequester) {
        if (definitionRequesters[label] === requester) {
            definitionRequesters.remove(label)
        }
    }

    fun hasDefinition(label: String): Boolean = definitionRequesters.containsKey(label)

    fun requestDefinition(label: String) {
        requestedDefinitions[label] = Unit
    }

    fun clearDefinitionRequest(label: String) {
        requestedDefinitions.remove(label)
    }

    fun isDefinitionRequested(label: String): Boolean = requestedDefinitions.containsKey(label)

    fun requestReference(label: String) {
        requestedReferences[label] = Unit
    }

    fun clearReferenceRequest(label: String) {
        requestedReferences.remove(label)
    }

    fun isReferenceRequested(label: String): Boolean = requestedReferences.containsKey(label)

    fun registerReference(label: String, requester: BringIntoViewRequester) {
        referenceRequesters.getOrPut(label) { mutableListOf() }.add(requester)
    }

    fun unregisterReference(label: String, requester: BringIntoViewRequester) {
        val requesters = referenceRequesters[label] ?: return
        requesters.removeAll { it === requester }
        if (requesters.isEmpty()) {
            referenceRequesters.remove(label)
        }
    }

    fun rememberReturnPosition(label: String, scrollValue: Int) {
        returnPositions[label] = FootnoteReturnPosition.Scroll(scrollValue)
    }

    fun rememberLazyListPosition(label: String, index: Int, offset: Int) {
        returnPositions[label] = FootnoteReturnPosition.LazyList(index, offset)
    }

    fun hasReturnPosition(label: String): Boolean = returnPositions.containsKey(label)

    fun getReturnPosition(label: String): FootnoteReturnPosition? = returnPositions[label]

    suspend fun bringDefinitionIntoView(label: String): Boolean {
        val requester = definitionRequesters[label] ?: return false
        requester.bringIntoView()
        return true
    }

    suspend fun bringReferenceIntoView(label: String): Boolean {
        val requester = referenceRequesters[label]?.firstOrNull() ?: return false
        requester.bringIntoView()
        return true
    }
}
