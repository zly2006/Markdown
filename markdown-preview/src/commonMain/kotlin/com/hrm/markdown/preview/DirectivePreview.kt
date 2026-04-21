package com.hrm.markdown.preview

import com.hrm.markdown.renderer.Markdown

internal val directivePreviewGroups = listOf(
    PreviewGroup(
        id = "block_directive",
        title = "块级指令",
        description = "{% tag args %}...{% endtag %} 块级指令语法",
        items = listOf(
            PreviewItem(
                id = "self_closing_directive",
                title = "自闭合指令",
                content = {
                    Markdown(
                        markdown = """
{% youtube abc123 %}

{% include file="header.html" cache=true %}
                        """.trimIndent()
                    )
                }
            ),
            PreviewItem(
                id = "directive_with_content",
                title = "包含内容的指令",
                content = {
                    Markdown(
                        markdown = """
{% alert %}
This is a warning message with **bold** text.
{% endalert %}

{% note %}
Important stuff here.

- Item 1
- Item 2
{% endnote %}
                        """.trimIndent()
                    )
                }
            ),
            PreviewItem(
                id = "directive_args",
                title = "参数解析",
                content = {
                    Markdown(
                        markdown = """
位置参数：

{% tag "hello world" %}

键值对参数：

{% include file="header.html" cache=true %}

混合参数：

{% video abc123 width=640 height=480 %}
                        """.trimIndent()
                    )
                }
            ),
        )
    ),
    PreviewGroup(
        id = "inline_directive",
        title = "行内指令",
        description = "段落内嵌入的 {% tag args %} 行内指令",
        items = listOf(
            PreviewItem(
                id = "inline_directive_basic",
                title = "行内指令",
                content = {
                    Markdown(
                        markdown = """
Text with {% icon name=star %} inside a paragraph.

Click {% button text="Submit" %} to continue.
                        """.trimIndent()
                    )
                }
            ),
        )
    ),
)
