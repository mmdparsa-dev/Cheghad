package com.mmdparsadev.cheghad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmdparsadev.cheghad.ui.theme.VazirFontFamily
import com.mmdparsadev.cheghad.ui.theme.defaultPlatformStyle

/**
 * Display text formatted as Markdown, compatible with Jetpack Compose and Cheghad theme
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    onLinkClick: ((String) -> Unit)? = null
) {
    MarkdownContent(
        markdown = markdown,
        modifier = modifier,
        color = color,
        linkColor = linkColor,
        onLinkClick = onLinkClick
    )
}

/**
 * Main renderer for Markdown components
 */
@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    onLinkClick: ((String) -> Unit)? = null
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    MarkdownHeadingView(
                        block = block,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        linkColor = linkColor,
                        onLinkClick = onLinkClick,
                        uriHandler = uriHandler
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    MarkdownParagraphView(
                        block = block,
                        color = color,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        linkColor = linkColor,
                        onLinkClick = onLinkClick,
                        uriHandler = uriHandler
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    MarkdownBulletItemView(
                        block = block,
                        color = color,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        linkColor = linkColor,
                        onLinkClick = onLinkClick,
                        uriHandler = uriHandler
                    )
                }
                is MarkdownBlock.NumberedItem -> {
                    MarkdownNumberedItemView(
                        block = block,
                        color = color,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        linkColor = linkColor,
                        onLinkClick = onLinkClick,
                        uriHandler = uriHandler
                    )
                }
                is MarkdownBlock.TaskItem -> {
                    MarkdownTaskItemView(
                        block = block,
                        color = color,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        linkColor = linkColor,
                        onLinkClick = onLinkClick,
                        uriHandler = uriHandler
                    )
                }
                is MarkdownBlock.Blockquote -> {
                    MarkdownBlockquoteView(
                        block = block,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        linkColor = linkColor,
                        onLinkClick = onLinkClick,
                        uriHandler = uriHandler
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    MarkdownCodeBlockView(block = block)
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

// ==================== Markdown Block Models ====================

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletItem(val depth: Int, val text: String) : MarkdownBlock
    data class NumberedItem(val depth: Int, val number: String, val text: String) : MarkdownBlock
    data class TaskItem(val depth: Int, val isChecked: Boolean, val text: String) : MarkdownBlock
    data class Blockquote(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock
    data object Divider : MarkdownBlock
}

/**
 * Parses markdown lines into structured block elements
 */
fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var i = 0
    val totalLines = lines.size

    var inCodeBlock = false
    var codeBlockLang: String? = null
    val codeBlockLines = mutableListOf<String>()

    while (i < totalLines) {
        val line = lines[i]
        val trimmed = line.trim()

        // 1. Fenced Code Block (```)
        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(
                    MarkdownBlock.CodeBlock(
                        language = codeBlockLang,
                        code = codeBlockLines.joinToString("\n")
                    )
                )
                codeBlockLines.clear()
                codeBlockLang = null
                inCodeBlock = false
            } else {
                inCodeBlock = true
                val lang = trimmed.removePrefix("```").trim()
                codeBlockLang = lang.ifEmpty { null }
            }
            i++
            continue
        }

        if (inCodeBlock) {
            codeBlockLines.add(line)
            i++
            continue
        }

        // 2. Blank line
        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // 3. Horizontal Rule / Divider (---, ***, ___)
        if (trimmed.matches(Regex("^(?:[-*_]\\s*){3,}$"))) {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // 4. Headings (# to ######)
        val headingMatch = Regex("^(#{1,6})\\s+(.*)$").matchEntire(trimmed)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val headingText = headingMatch.groupValues[2].trim()
            blocks.add(MarkdownBlock.Heading(level, headingText))
            i++
            continue
        }

        // 5. Checklist / Task Item (- [ ] or - [x])
        val taskMatch = Regex("^(\\s*)[-*+]\\s+\\[([ xX])\\]\\s+(.*)$").matchEntire(line)
        if (taskMatch != null) {
            val depth = (taskMatch.groupValues[1].length / 2).coerceIn(0, 4)
            val isChecked = taskMatch.groupValues[2].equals("x", ignoreCase = true)
            val taskText = taskMatch.groupValues[3].trim()
            blocks.add(MarkdownBlock.TaskItem(depth, isChecked, taskText))
            i++
            continue
        }

        // 6. Unordered List Item (*, -, +)
        val bulletMatch = Regex("^(\\s*)[-*+]\\s+(.*)$").matchEntire(line)
        if (bulletMatch != null) {
            val depth = (bulletMatch.groupValues[1].length / 2).coerceIn(0, 4)
            val bulletText = bulletMatch.groupValues[2].trim()
            blocks.add(MarkdownBlock.BulletItem(depth, bulletText))
            i++
            continue
        }

        // 7. Ordered List Item (1., 2., etc.)
        val numberedMatch = Regex("^(\\s*)(\\d+)[.)]\\s+(.*)$").matchEntire(line)
        if (numberedMatch != null) {
            val depth = (numberedMatch.groupValues[1].length / 2).coerceIn(0, 4)
            val number = numberedMatch.groupValues[2]
            val itemText = numberedMatch.groupValues[3].trim()
            blocks.add(MarkdownBlock.NumberedItem(depth, number, itemText))
            i++
            continue
        }

        // 8. Blockquote (> ...)
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            quoteLines.add(trimmed.removePrefix(">").trim())
            i++
            while (i < totalLines) {
                val nextTrimmed = lines[i].trim()
                if (nextTrimmed.startsWith(">")) {
                    quoteLines.add(nextTrimmed.removePrefix(">").trim())
                    i++
                } else if (nextTrimmed.isNotEmpty() && !nextTrimmed.startsWith("#") && !nextTrimmed.startsWith("```")) {
                    quoteLines.add(nextTrimmed)
                    i++
                } else {
                    break
                }
            }
            blocks.add(MarkdownBlock.Blockquote(quoteLines.joinToString("\n")))
            continue
        }

        // 9. Paragraph / Normal text (merge continuation lines)
        val paragraphLines = mutableListOf<String>()
        paragraphLines.add(trimmed)
        i++
        while (i < totalLines) {
            val nextLine = lines[i]
            val nextTrimmed = nextLine.trim()
            if (nextTrimmed.isEmpty() ||
                nextTrimmed.startsWith("#") ||
                nextTrimmed.startsWith("```") ||
                nextTrimmed.startsWith(">") ||
                nextTrimmed.matches(Regex("^(?:[-*_]\\s*){3,}$")) ||
                Regex("^(\\s*)[-*+]\\s+").containsMatchIn(nextLine) ||
                Regex("^(\\s*)(\\d+)[.)]\\s+").containsMatchIn(nextLine)
            ) {
                break
            }
            paragraphLines.add(nextTrimmed)
            i++
        }
        blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString(" ")))
    }

    if (inCodeBlock && codeBlockLines.isNotEmpty()) {
        blocks.add(
            MarkdownBlock.CodeBlock(
                language = codeBlockLang,
                code = codeBlockLines.joinToString("\n")
            )
        )
    }

    return blocks
}

// ==================== Inline Style Parser ====================

fun buildMarkdownAnnotatedString(
    text: String,
    primaryColor: Color,
    linkColor: Color,
    codeBgColor: Color,
    codeTextColor: Color,
    onLinkClick: ((String) -> Unit)? = null,
    uriHandler: UriHandler? = null
): AnnotatedString {
    return buildAnnotatedString {
        parseInline(
            text = text,
            start = 0,
            end = text.length,
            primaryColor = primaryColor,
            linkColor = linkColor,
            codeBgColor = codeBgColor,
            codeTextColor = codeTextColor,
            onLinkClick = onLinkClick,
            uriHandler = uriHandler
        )
    }
}

private fun AnnotatedString.Builder.parseInline(
    text: String,
    start: Int,
    end: Int,
    primaryColor: Color,
    linkColor: Color,
    codeBgColor: Color,
    codeTextColor: Color,
    onLinkClick: ((String) -> Unit)?,
    uriHandler: UriHandler?
) {
    var index = start
    while (index < end) {
        val ch = text[index]

        // 1. Inline code: `code`
        if (ch == '`') {
            val closeIndex = text.indexOf('`', index + 1)
            if (closeIndex in (index + 1) until end) {
                val codeContent = text.substring(index + 1, closeIndex)
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        background = codeBgColor,
                        color = codeTextColor
                    )
                )
                append(" $codeContent ")
                pop()
                index = closeIndex + 1
                continue
            }
        }

        // 2. Titled link: [label](url)
        if (ch == '[') {
            val closeBracket = text.indexOf(']', index + 1)
            if (closeBracket in (index + 1) until end && closeBracket + 1 < end && text[closeBracket + 1] == '(') {
                val closeParen = text.indexOf(')', closeBracket + 2)
                if (closeParen in (closeBracket + 2)..end) {
                    val label = text.substring(index + 1, closeBracket)
                    val url = text.substring(closeBracket + 2, closeParen).trim()

                    val linkAnnotation = LinkAnnotation.Url(
                        url = url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium
                            )
                        ),
                        linkInteractionListener = LinkInteractionListener { annotation ->
                            if (annotation is LinkAnnotation.Url) {
                                if (onLinkClick != null) {
                                    onLinkClick(annotation.url)
                                } else {
                                    try {
                                        uriHandler?.openUri(annotation.url)
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    )

                    pushLink(linkAnnotation)
                    parseInline(
                        text = label,
                        start = 0,
                        end = label.length,
                        primaryColor = primaryColor,
                        linkColor = linkColor,
                        codeBgColor = codeBgColor,
                        codeTextColor = codeTextColor,
                        onLinkClick = onLinkClick,
                        uriHandler = uriHandler
                    )
                    pop()
                    index = closeParen + 1
                    continue
                }
            }
        }

        // 3. Strikethrough: ~~text~~
        if (ch == '~' && index + 1 < end && text[index + 1] == '~') {
            val closeIndex = text.indexOf("~~", index + 2)
            if (closeIndex in (index + 2) until end) {
                pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                parseInline(
                    text = text.substring(index + 2, closeIndex),
                    start = 0,
                    end = closeIndex - (index + 2),
                    primaryColor = primaryColor,
                    linkColor = linkColor,
                    codeBgColor = codeBgColor,
                    codeTextColor = codeTextColor,
                    onLinkClick = onLinkClick,
                    uriHandler = uriHandler
                )
                pop()
                index = closeIndex + 2
                continue
            }
        }

        // 4. Bold + Italic: ***text*** or ___text___
        if ((ch == '*' || ch == '_') && index + 2 < end && text[index + 1] == ch && text[index + 2] == ch) {
            val delim = "$ch$ch$ch"
            val closeIndex = text.indexOf(delim, index + 3)
            if (closeIndex in (index + 3) until end) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                parseInline(
                    text = text.substring(index + 3, closeIndex),
                    start = 0,
                    end = closeIndex - (index + 3),
                    primaryColor = primaryColor,
                    linkColor = linkColor,
                    codeBgColor = codeBgColor,
                    codeTextColor = codeTextColor,
                    onLinkClick = onLinkClick,
                    uriHandler = uriHandler
                )
                pop()
                index = closeIndex + 3
                continue
            }
        }

        // 5. Bold: **text** or __text__
        if ((ch == '*' || ch == '_') && index + 1 < end && text[index + 1] == ch) {
            val delim = "$ch$ch"
            val closeIndex = text.indexOf(delim, index + 2)
            if (closeIndex in (index + 2) until end) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                parseInline(
                    text = text.substring(index + 2, closeIndex),
                    start = 0,
                    end = closeIndex - (index + 2),
                    primaryColor = primaryColor,
                    linkColor = linkColor,
                    codeBgColor = codeBgColor,
                    codeTextColor = codeTextColor,
                    onLinkClick = onLinkClick,
                    uriHandler = uriHandler
                )
                pop()
                index = closeIndex + 2
                continue
            }
        }

        // 6. Italic: *text* or _text_
        if (ch == '*') {
            val closeIndex = text.indexOf('*', index + 1)
            if (closeIndex in (index + 1) until end) {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                parseInline(
                    text = text.substring(index + 1, closeIndex),
                    start = 0,
                    end = closeIndex - (index + 1),
                    primaryColor = primaryColor,
                    linkColor = linkColor,
                    codeBgColor = codeBgColor,
                    codeTextColor = codeTextColor,
                    onLinkClick = onLinkClick,
                    uriHandler = uriHandler
                )
                pop()
                index = closeIndex + 1
                continue
            }
        }

        if (ch == '_') {
            val isStartOfWord = index == start || text[index - 1].isWhitespace()
            if (isStartOfWord) {
                val closeIndex = text.indexOf('_', index + 1)
                if (closeIndex in (index + 1) until end) {
                    val isEndOfWord = closeIndex + 1 == end || text[closeIndex + 1].isWhitespace() || !text[closeIndex + 1].isLetterOrDigit()
                    if (isEndOfWord) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        parseInline(
                            text = text.substring(index + 1, closeIndex),
                            start = 0,
                            end = closeIndex - (index + 1),
                            primaryColor = primaryColor,
                            linkColor = linkColor,
                            codeBgColor = codeBgColor,
                            codeTextColor = codeTextColor,
                            onLinkClick = onLinkClick,
                            uriHandler = uriHandler
                        )
                        pop()
                        index = closeIndex + 1
                        continue
                    }
                }
            }
        }

        // 7. Direct link URL: http:// or https://
        if (text.startsWith("http://", index) || text.startsWith("https://", index)) {
            var urlEnd = index
            while (urlEnd < end && !text[urlEnd].isWhitespace() && text[urlEnd] !in "()<>[]\"'") {
                urlEnd++
            }
            while (urlEnd > index && text[urlEnd - 1] in ".,;:!?") {
                urlEnd--
            }
            if (urlEnd > index) {
                val rawUrl = text.substring(index, urlEnd)
                val linkAnnotation = LinkAnnotation.Url(
                    url = rawUrl,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        )
                    ),
                    linkInteractionListener = LinkInteractionListener { annotation ->
                        if (annotation is LinkAnnotation.Url) {
                            if (onLinkClick != null) {
                                onLinkClick(annotation.url)
                            } else {
                                try {
                                    uriHandler?.openUri(annotation.url)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                )
                pushLink(linkAnnotation)
                append(rawUrl)
                pop()
                index = urlEnd
                continue
            }
        }

        // 8. GitHub mention: @username
        if (ch == '@' && (index == start || text[index - 1].isWhitespace() || text[index - 1] in "([<")) {
            var mentionEnd = index + 1
            while (mentionEnd < end && (text[mentionEnd].isLetterOrDigit() || text[mentionEnd] == '-' || text[mentionEnd] == '_')) {
                mentionEnd++
            }
            if (mentionEnd > index + 1) {
                val username = text.substring(index + 1, mentionEnd)
                val profileUrl = "https://github.com/$username"
                val linkAnnotation = LinkAnnotation.Url(
                    url = profileUrl,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    ),
                    linkInteractionListener = LinkInteractionListener { annotation ->
                        if (annotation is LinkAnnotation.Url) {
                            if (onLinkClick != null) {
                                onLinkClick(annotation.url)
                            } else {
                                try {
                                    uriHandler?.openUri(annotation.url)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                )
                pushLink(linkAnnotation)
                append("@$username")
                pop()
                index = mentionEnd
                continue
            }
        }

        // 9. GitHub Issue / PR reference: #123
        if (ch == '#' && (index == start || text[index - 1].isWhitespace() || text[index - 1] in "([<")) {
            var issueEnd = index + 1
            while (issueEnd < end && text[issueEnd].isDigit()) {
                issueEnd++
            }
            if (issueEnd > index + 1) {
                val issueNum = text.substring(index, issueEnd)
                pushStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold))
                append(issueNum)
                pop()
                index = issueEnd
                continue
            }
        }

        // 10. Normal character
        append(ch)
        index++
    }
}

// ==================== Block Visual Components ====================

@Composable
private fun MarkdownHeadingView(
    block: MarkdownBlock.Heading,
    primaryColor: Color,
    linkColor: Color,
    onLinkClick: ((String) -> Unit)?,
    uriHandler: UriHandler
) {
    val fontSize = when (block.level) {
        1 -> 17.sp
        2 -> 15.sp
        3 -> 14.sp
        4 -> 13.5.sp
        else -> 12.5.sp
    }
    val fontWeight = when (block.level) {
        1, 2 -> FontWeight.Bold
        3, 4 -> FontWeight.SemiBold
        else -> FontWeight.Medium
    }
    val fontColor = when (block.level) {
        1, 3 -> primaryColor
        2 -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val topPadding = when (block.level) {
        1 -> 10.dp
        2 -> 8.dp
        3 -> 6.dp
        4 -> 4.dp
        else -> 2.dp
    }

    val annotated = remember(block.text, primaryColor, linkColor) {
        buildMarkdownAnnotatedString(
            text = block.text,
            primaryColor = primaryColor,
            linkColor = linkColor,
            codeBgColor = primaryColor.copy(alpha = 0.12f),
            codeTextColor = primaryColor,
            onLinkClick = onLinkClick,
            uriHandler = uriHandler
        )
    }

    Text(
        text = annotated,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = fontColor,
        fontFamily = VazirFontFamily,
        style = LocalTextStyle.current.copy(
            textDirection = TextDirection.Content,
            platformStyle = defaultPlatformStyle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 2.dp)
    )
}

@Composable
private fun MarkdownParagraphView(
    block: MarkdownBlock.Paragraph,
    color: Color,
    primaryColor: Color,
    linkColor: Color,
    onLinkClick: ((String) -> Unit)?,
    uriHandler: UriHandler
) {
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(block.text, primaryColor, linkColor, codeBgColor, codeTextColor) {
        buildMarkdownAnnotatedString(
            text = block.text,
            primaryColor = primaryColor,
            linkColor = linkColor,
            codeBgColor = codeBgColor,
            codeTextColor = codeTextColor,
            onLinkClick = onLinkClick,
            uriHandler = uriHandler
        )
    }

    Text(
        text = annotated,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        color = color,
        fontFamily = VazirFontFamily,
        style = LocalTextStyle.current.copy(
            textDirection = TextDirection.Content,
            platformStyle = defaultPlatformStyle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}

@Composable
private fun MarkdownBulletItemView(
    block: MarkdownBlock.BulletItem,
    color: Color,
    primaryColor: Color,
    linkColor: Color,
    onLinkClick: ((String) -> Unit)?,
    uriHandler: UriHandler
) {
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(block.text, primaryColor, linkColor, codeBgColor, codeTextColor) {
        buildMarkdownAnnotatedString(
            text = block.text,
            primaryColor = primaryColor,
            linkColor = linkColor,
            codeBgColor = codeBgColor,
            codeTextColor = codeTextColor,
            onLinkClick = onLinkClick,
            uriHandler = uriHandler
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (block.depth * 14).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp, end = 8.dp, start = 2.dp)
                .size(5.dp)
                .background(primaryColor, CircleShape)
        )
        Text(
            text = annotated,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = color,
            fontFamily = VazirFontFamily,
            style = LocalTextStyle.current.copy(
                textDirection = TextDirection.Content,
                platformStyle = defaultPlatformStyle
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownNumberedItemView(
    block: MarkdownBlock.NumberedItem,
    color: Color,
    primaryColor: Color,
    linkColor: Color,
    onLinkClick: ((String) -> Unit)?,
    uriHandler: UriHandler
) {
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(block.text, primaryColor, linkColor, codeBgColor, codeTextColor) {
        buildMarkdownAnnotatedString(
            text = block.text,
            primaryColor = primaryColor,
            linkColor = linkColor,
            codeBgColor = codeBgColor,
            codeTextColor = codeTextColor,
            onLinkClick = onLinkClick,
            uriHandler = uriHandler
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (block.depth * 14).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${block.number}.",
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            fontFamily = VazirFontFamily,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = annotated,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = color,
            fontFamily = VazirFontFamily,
            style = LocalTextStyle.current.copy(
                textDirection = TextDirection.Content,
                platformStyle = defaultPlatformStyle
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownTaskItemView(
    block: MarkdownBlock.TaskItem,
    color: Color,
    primaryColor: Color,
    linkColor: Color,
    onLinkClick: ((String) -> Unit)?,
    uriHandler: UriHandler
) {
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(block.text, primaryColor, linkColor, codeBgColor, codeTextColor) {
        buildMarkdownAnnotatedString(
            text = block.text,
            primaryColor = primaryColor,
            linkColor = linkColor,
            codeBgColor = codeBgColor,
            codeTextColor = codeTextColor,
            onLinkClick = onLinkClick,
            uriHandler = uriHandler
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (block.depth * 14).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp, end = 8.dp, start = 2.dp)
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (block.isChecked) primaryColor
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = 1.dp,
                    color = if (block.isChecked) primaryColor
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (block.isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Text(
            text = annotated,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = if (block.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else color,
            textDecoration = if (block.isChecked) TextDecoration.LineThrough else null,
            fontFamily = VazirFontFamily,
            style = LocalTextStyle.current.copy(
                textDirection = TextDirection.Content,
                platformStyle = defaultPlatformStyle
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownBlockquoteView(
    block: MarkdownBlock.Blockquote,
    primaryColor: Color,
    linkColor: Color,
    onLinkClick: ((String) -> Unit)?,
    uriHandler: UriHandler
) {
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant
    val codeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(block.text, primaryColor, linkColor, codeBgColor, codeTextColor) {
        buildMarkdownAnnotatedString(
            text = block.text,
            primaryColor = primaryColor,
            linkColor = linkColor,
            codeBgColor = codeBgColor,
            codeTextColor = codeTextColor,
            onLinkClick = onLinkClick,
            uriHandler = uriHandler
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(20.dp)
                    .background(primaryColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = annotated,
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = VazirFontFamily,
                style = LocalTextStyle.current.copy(
                    textDirection = TextDirection.Content,
                    platformStyle = defaultPlatformStyle
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MarkdownCodeBlockView(
    block: MarkdownBlock.CodeBlock
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            if (!block.language.isNullOrBlank()) {
                Text(
                    text = block.language.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = block.code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
