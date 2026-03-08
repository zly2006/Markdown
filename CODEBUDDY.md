# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## Project Overview

A Kotlin Multiplatform (KMP) Markdown parsing and rendering library using Compose Multiplatform. Targets Android, iOS, Desktop (JVM), Web (Wasm/JS). Kotlin 2.3.10 + Compose Multiplatform 1.10.1.

## Commands

### Build & Run
```bash
# Run Desktop Demo app
./gradlew :composeApp:run

# Build Android APK
./gradlew :composeApp:assembleDebug

# Run Web (Wasm) dev server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Run Web (JS) dev server
./gradlew :composeApp:jsBrowserDevelopmentRun
```

### Testing
```bash
# Parser module tests only
./gradlew :markdown-parser:jvmTest

# Renderer module tests only
./gradlew :markdown-renderer:jvmTest

# All tests across all modules
./gradlew jvmTest

# Run a single test class (example)
./gradlew :markdown-parser:jvmTest --tests "com.hrm.markdown.parser.HeadingParserTest"

# Run a single test method (example)
./gradlew :markdown-parser:jvmTest --tests "com.hrm.markdown.parser.HeadingParserTest.should_parse_heading_correctly"
```

All tests must pass (BUILD SUCCESSFUL, exit code 0) before committing.

## Architecture

### Module Dependency Chain

```
markdown-parser  (core parsing, zero UI deps, only kotlinx-coroutines)
       ↑
markdown-renderer  (Compose UI rendering, depends on parser + Coil3 + Ktor3 + LaTeX)
       ↑
markdown-preview  (categorized demo data & navigation UI, depends on renderer)
       ↑
composeApp  (thin shell: platform entry points + MaterialTheme, depends on preview)
       ↑
androidapp  (Android-only entry)
```

### markdown-parser — AST-based Recursive Descent Parser

**Entry point**: `MarkdownParser` — facade class exposing three parsing modes:
- `parse(input)` — full parse
- `beginStream()`/`append(chunk)`/`endStream()` — LLM streaming
- `insert()`/`delete()`/`replace()` — editor incremental

**Parsing pipeline** (end-to-end):

1. **SourceText**: Normalizes line endings, replaces NUL, builds line offset index for O(1) line lookup, computes FNV-1a content hashes.

2. **BlockParser** (~1400 lines): Two-pass CommonMark algorithm.
   - Pass 1 (block structure): Iterates lines, manages an `openBlocks` stack. For each line: continuation check → lazy continuation → close unmatched blocks → `BlockStarterRegistry.tryStart()` to open new blocks → add line content to tip.
   - Pass 2 (lazy inline): Sets up deferred inline parsing on container nodes (Paragraph, Heading, TableCell, etc.) via `setLazyInlineContent()`. Inline parsing triggers only on first `children` access.

3. **InlineParser** (~1470 lines): CommonMark delimiter algorithm using a doubly-linked list + delimiter stack + bracket stack. Handles emphasis, strikethrough, highlight, links, images, code spans, math, autolinks, HTML entities, escapes, emoji, etc.

4. **PostProcessor pipeline**: Runs after AST construction in priority order — HeadingIdProcessor (slug IDs) → BlockAttributeProcessor ({.class #id}) → AbbreviationProcessor → DiagramProcessor (mermaid/plantuml) → ColumnsLayoutProcessor → HtmlFilterProcessor.

**AST node hierarchy**:
- `Node` (sealed) → `ContainerNode` (sealed, has children, supports lazy inline parsing) | `LeafNode` (sealed, has literal)
- ~30 block node types (Document, Heading, Paragraph, FencedCodeBlock, ListBlock, Table, BlockQuote, Admonition, MathBlock, etc.)
- ~20 inline node types (Text, Emphasis, StrongEmphasis, Link, Image, InlineCode, InlineMath, FootnoteReference, Emoji, etc.)
- Each node carries `sourceRange`, `lineRange`, `contentHash` (FNV-1a), `stableKey` (for Compose keying)

**Flavour system** — controls which syntax features are enabled:
- `MarkdownFlavour` interface defines `blockStarters`, `postProcessors`, `enableGfmAutolinks`, `enableExtendedInline`
- `CommonMarkFlavour` — 8 BlockStarters, standard spec
- `GFMFlavour` — CommonMark + tables + strikethrough + autolinks, removes indented code blocks
- `ExtendedFlavour` — GFM + 15 BlockStarters + 6 PostProcessors (footnotes, math, frontmatter, definition lists, custom containers, diagrams, emoji, highlight, super/subscript, etc.)
- `FlavourCache` — singleton cache for built-in flavours, per-reference for custom flavours

**Incremental parsing** (`IncrementalEngine`): Unified engine for all three modes.
- Edit mode: `DirtyRegionTracker` maps edits to line ranges expanded to block boundaries → `BlockParser.parseLines(dirtyStart, dirtyEnd)` → `NodeReuser` matches prefix/suffix nodes by line range + contentHash verification → assembles new Document = reused prefix + fresh blocks + reused suffix.
- Streaming mode: Tail-block stability detection + `InlineAutoCloser` auto-completes unclosed inline syntax for LLM partial output.

**Plugin architecture**: `BlockStarter` interface (priority + `tryStart()`) in `block/starters/` — one file per block type. `PostProcessor` interface (priority + `process()`) in `block/postprocessors/`. Custom flavours compose by selecting starters and processors.

### markdown-renderer — Compose Multiplatform Rendering

**Entry point**: `Markdown()` composable — accepts markdown text, theme, streaming flag, callbacks.

**Rendering pipeline**:
1. `rememberStreamingDocument()` — manages parse state: async full parse (Dispatchers.Default) for non-streaming, incremental `append()` for streaming.
2. `InnerMarkdown()` — rendering throttle (100ms sampling during streaming, ~10fps), structural equality check (`===` reference compare per node) to skip unnecessary recomposition, optional pagination (initial N blocks + load-more on scroll to 80%).
3. `BlockRenderer()` — `when` dispatcher routing ~20 AST node types to dedicated renderers in `block/` package.
4. `InlineRenderer` (`rememberInlineContent()`) — recursively builds `AnnotatedString` + `InlineTextContent` map from inline AST nodes. Called by block renderers like `ParagraphRenderer`.

**Block vs Inline distinction**:
- Block elements → independent Composable components (Column, Row, Box, Canvas)
- Inline elements → SpanStyle annotations within AnnotatedString, rendered via `BasicText`
- `ParagraphRenderer` bridges the two: detects images → splits into `TextRun`/`ImageItem` segments for mixed content

**Theming**: `MarkdownTheme` (@Immutable data class, 40+ properties) with `light()`/`dark()`/`auto()` factories. Distributed via `LocalMarkdownTheme` CompositionLocal. Includes `SyntaxColorScheme` for code highlighting.

**Image loading**: Coil3 `AsyncImage` default, replaceable via `imageContent` parameter. Platform-specific Ktor3 engines (Android/Darwin/Java/JS). `MixedParagraphRenderer` handles images inside paragraphs by splitting into block-level image components.

**Code highlighting**: `SyntaxHighlighter` — regex-driven engine supporting 20+ languages, 6 token types (keyword, string, comment, number, type, function).

**Diagram rendering**: Native Canvas drawing for Mermaid flowcharts, Mermaid sequence diagrams, PlantUML sequence diagrams, Graphviz DOT graphs — no external dependencies.

**Streaming optimizations** (4 layers): incremental parsing → reference-equality structural comparison → 100ms render throttle + skip SelectionContainer → `stableKey` (line-number based, no contentHash) prevents Compose destroy/recreate.

### markdown-preview — Demo Data & Navigation

A KMP library (not an application) providing categorized preview content. Three-level navigation: `PreviewCategory` → `PreviewGroup` → `PreviewItem`.

**Adding previews**: Edit files in `markdown-preview/src/commonMain/kotlin/com/hrm/markdown/preview/`. To add items to existing categories, append `PreviewItem` to the relevant `*Preview.kt` file. To add a new category, create a new `*Preview.kt` file with a `val` of `List<PreviewGroup>`, then register it in `previewCategories` in `MarkdownPreview.kt`. No changes needed in `composeApp`.

### composeApp — Platform Shell

Minimal shell (~28 lines of shared code in `App.kt`): `MaterialTheme` + `Surface` + `MarkdownPreview()`. Platform entry points in `jvmMain/`, `webMain/`, `iosMain/`. All demo content lives in `markdown-preview`.

## Development Workflow for New Features

1. **Parser**: Implement parsing logic in `markdown-parser`. Add `BlockStarter` in `block/starters/` or update `InlineParser`. Register in the appropriate `Flavour`.
2. **Parser Tests**: Add test class in `markdown-parser/src/commonTest/kotlin/com/hrm/markdown/parser/`. Cover happy path, edge cases, and error handling. Use `kotlin.test` framework.
3. **Renderer**: Add block renderer in `markdown-renderer/.../block/` or update `InlineRenderer` in `inline/`. Register in `BlockRenderer`'s `when` dispatcher.
4. **Renderer Tests**: Add tests in `markdown-renderer/src/commonTest/kotlin/com/hrm/markdown/renderer/`.
5. **Preview**: Add preview items in `markdown-preview/.../preview/` — either in existing `*Preview.kt` or as a new category registered in `MarkdownPreview.kt`.
6. **Coverage doc**: Update `markdown-parser/PARSER_COVERAGE_ANALYSIS.md` — mark supported features ✅, update coverage stats.
7. **Verify**: Run `./gradlew jvmTest` — must show BUILD SUCCESSFUL.

## Key Conventions

- Package root: `com.hrm.markdown.parser` (parser), `com.hrm.markdown.renderer` (renderer), `com.hrm.markdown.preview` (preview)
- AST nodes are sealed classes; `ContainerNode` supports lazy inline parsing
- `stableKey` on AST nodes is line-number based (not content-based) to prevent Compose flicker during streaming
- `contentHash` (FNV-1a) is used for incremental node reuse verification, not for identity
- Test naming: `should_expectedBehavior_when_condition`
- Flavour system is the extension mechanism — never modify `BlockParser`/`InlineParser` directly for new syntax; add a `BlockStarter` or `PostProcessor` instead
