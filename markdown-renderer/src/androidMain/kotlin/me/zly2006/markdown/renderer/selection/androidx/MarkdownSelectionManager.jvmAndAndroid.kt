@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

/*
 * Copyright 2025 The Android Open Source Project
 * Copyright (c) 2026 Liyan Zhao (zly2006)
 * Modified by Liyan Zhao in 2026: package relocation, persistent selection, and Compose 1.12 platform ABI adaptation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.zly2006.markdown.renderer.selection.androidx

import androidx.compose.foundation.text.selection.Selectable
import androidx.compose.foundation.text.selection.SelectionLayoutBuilder
import me.zly2006.markdown.renderer.selection.DocumentOrderedSelectable

internal actual val MarkdownSelectionManager.skipMarkdownCopyKeyEvent: Boolean
    get() = false

internal actual val isNewMarkdownContextMenuSupported: Boolean = true

internal actual fun createMarkdownSelectableAdapter(
    selectable: Selectable,
    orderedSelectable: DocumentOrderedSelectable,
): MarkdownSelectable = object : MarkdownSelectableAdapter(selectable, orderedSelectable) {
    override fun appendSelectableInfoToBuilder(builder: SelectionLayoutBuilder) {
        this.selectable.appendSelectableInfoToBuilder(builder)
    }
}

internal actual fun MarkdownSelectable.appendToSelectionLayout(
    builder: SelectionLayoutBuilder,
    isLast: Boolean,
) {
    appendSelectableInfoToBuilder(builder)
}
