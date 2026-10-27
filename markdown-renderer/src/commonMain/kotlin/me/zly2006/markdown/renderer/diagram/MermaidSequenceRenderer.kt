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

package me.zly2006.markdown.renderer.diagram

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

// parses mermaid sequencediagram syntax into shared SequenceDiagramData
internal fun parseMermaidSequence(code: String): SequenceDiagramData? {
    val lines = code.lines().map { it.trim() }.filter { it.isNotBlank() }

    val participantsMap = mutableMapOf<String, Participant>()
    val messages = mutableListOf<SeqMessage>()

    for (line in lines) {
        if (line.startsWith("sequenceDiagram", ignoreCase = true)) continue

        // participant/actor declaration
        val actorMatch = MERMAID_ACTOR_RE.find(line)
        if (actorMatch != null) {
            val id = actorMatch.groupValues[1]
            val label = actorMatch.groupValues[2].ifBlank { id }
            participantsMap[id] = Participant(id, label, ParticipantType.ACTOR)
            continue
        }

        val participantMatch = MERMAID_PARTICIPANT_RE.find(line)
        if (participantMatch != null) {
            val id = participantMatch.groupValues[1]
            val label = participantMatch.groupValues[2].ifBlank { id }
            participantsMap[id] = Participant(id, label)
            continue
        }

        // messages: A->>B: label  or  A-->>B: label  etc
        val msgMatch = MERMAID_MSG_RE.find(line)
        if (msgMatch != null) {
            val from = msgMatch.groupValues[1].trim()
            val arrow = msgMatch.groupValues[2].trim()
            val to = msgMatch.groupValues[3].trim()
            val label = msgMatch.groupValues[4].trim()

            val arrowStyle = when (arrow) {
                "-->>" -> ArrowStyle.DOTTED_OPEN
                "-->" -> ArrowStyle.DOTTED
                "->>" -> ArrowStyle.SOLID_OPEN
                else -> ArrowStyle.SOLID
            }

            if (from !in participantsMap) {
                participantsMap[from] = Participant(from, from)
            }
            if (to !in participantsMap) {
                participantsMap[to] = Participant(to, to)
            }
            messages.add(SeqMessage(from, to, label, arrowStyle))
            continue
        }
    }

    if (participantsMap.isEmpty() && messages.isEmpty()) return null
    return SequenceDiagramData(participantsMap.values.toList(), messages)
}

private val MERMAID_ACTOR_RE = Regex("""^actor\s+(\S+)(?:\s+as\s+(.+))?$""", RegexOption.IGNORE_CASE)
private val MERMAID_PARTICIPANT_RE = Regex("""^participant\s+(\S+)(?:\s+as\s+(.+))?$""", RegexOption.IGNORE_CASE)

// matches: Alice->>Bob: Hello  or  Alice-->>Bob: Hi  etc
private val MERMAID_MSG_RE = Regex("""^(.+?)\s*(-->>|-->|->(?:>)?|-x|--x)\s*(.+?)\s*:\s*(.*)$""")

// parses mermaid syntax then reuses shared sequence diagram drawing engine
@Composable
internal fun MermaidSequenceDiagram(
    code: String,
    modifier: Modifier = Modifier,
) {
    val data = remember(code) { parseMermaidSequence(code) }
    if (data == null) {
        DiagramFallback(code, "Mermaid Sequence", modifier)
        return
    }
    // reuse shared sequence diagram renderer
    SequenceDiagramRenderer(data, modifier)
}
