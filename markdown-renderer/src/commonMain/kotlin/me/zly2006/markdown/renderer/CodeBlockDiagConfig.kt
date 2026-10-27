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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 代码块诊断配置，用于控制变量法定位流式抖动来源。
 *
 * 通过 [LocalCodeBlockDiagConfig] 在组件树中传递，
 * [me.zly2006.markdown.renderer.block.CodeBlockCanvas] 消费这些配置来切换不同的渲染路径。
 *
 * **仅用于开发调试，生产环境使用默认值即可。**
 */
@Immutable
data class CodeBlockDiagConfig(
    /** 开关③：替换代码块为固定高度的占位框（不渲染任何代码内容） */
    val usePlaceholder: Boolean = false,
    /** 开关④：去掉透明 Text 层（禁用选中复制能力，排除 Text 层对布局的影响） */
    val disableSelectionLayer: Boolean = false,
    /** 开关⑤：固定代码块高度（300dp），排除高度变化传导到外层的影响 */
    val useFixedHeight: Boolean = false,
)

val LocalCodeBlockDiagConfig = staticCompositionLocalOf { CodeBlockDiagConfig() }

@Composable
fun ProvideCodeBlockDiagConfig(
    config: CodeBlockDiagConfig,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCodeBlockDiagConfig provides config) {
        content()
    }
}
