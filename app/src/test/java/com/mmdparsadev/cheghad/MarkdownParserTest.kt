package com.mmdparsadev.cheghad

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mmdparsadev.cheghad.ui.MarkdownBlock
import com.mmdparsadev.cheghad.ui.buildMarkdownAnnotatedString
import com.mmdparsadev.cheghad.ui.parseMarkdownBlocks
import org.junit.Assert.*
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun testParseHeadings() {
        val markdown = """
            # Heading 1
            ## Heading 2
            ### Heading 3
            #### Heading 4
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(4, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Heading && (blocks[0] as MarkdownBlock.Heading).level == 1)
        assertEquals("Heading 1", (blocks[0] as MarkdownBlock.Heading).text)

        assertTrue(blocks[1] is MarkdownBlock.Heading && (blocks[1] as MarkdownBlock.Heading).level == 2)
        assertEquals("Heading 2", (blocks[1] as MarkdownBlock.Heading).text)

        assertTrue(blocks[2] is MarkdownBlock.Heading && (blocks[2] as MarkdownBlock.Heading).level == 3)
        assertTrue(blocks[3] is MarkdownBlock.Heading && (blocks[3] as MarkdownBlock.Heading).level == 4)
    }

    @Test
    fun testParseListsAndTasks() {
        val markdown = """
            * Unordered item 1
            - Unordered item 2
              * Indented item
            1. Numbered item 1
            2. Numbered item 2
            - [ ] Task incomplete
            - [x] Task complete
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(7, blocks.size)

        assertTrue(blocks[0] is MarkdownBlock.BulletItem && (blocks[0] as MarkdownBlock.BulletItem).depth == 0)
        assertEquals("Unordered item 1", (blocks[0] as MarkdownBlock.BulletItem).text)

        assertTrue(blocks[1] is MarkdownBlock.BulletItem && (blocks[1] as MarkdownBlock.BulletItem).depth == 0)
        assertTrue(blocks[2] is MarkdownBlock.BulletItem && (blocks[2] as MarkdownBlock.BulletItem).depth == 1)

        assertTrue(blocks[3] is MarkdownBlock.NumberedItem && (blocks[3] as MarkdownBlock.NumberedItem).number == "1")
        assertEquals("Numbered item 1", (blocks[3] as MarkdownBlock.NumberedItem).text)

        assertTrue(blocks[5] is MarkdownBlock.TaskItem && !(blocks[5] as MarkdownBlock.TaskItem).isChecked)
        assertEquals("Task incomplete", (blocks[5] as MarkdownBlock.TaskItem).text)

        assertTrue(blocks[6] is MarkdownBlock.TaskItem && (blocks[6] as MarkdownBlock.TaskItem).isChecked)
        assertEquals("Task complete", (blocks[6] as MarkdownBlock.TaskItem).text)
    }

    @Test
    fun testParseCodeBlocksAndBlockquotes() {
        val markdown = """
            > This is a quote
            > second line of quote

            ```kotlin
            val x = 10
            println(x)
            ```

            ---
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(3, blocks.size)

        assertTrue(blocks[0] is MarkdownBlock.Blockquote)
        assertTrue((blocks[0] as MarkdownBlock.Blockquote).text.contains("This is a quote"))
        assertTrue((blocks[0] as MarkdownBlock.Blockquote).text.contains("second line of quote"))

        assertTrue(blocks[1] is MarkdownBlock.CodeBlock)
        assertEquals("kotlin", (blocks[1] as MarkdownBlock.CodeBlock).language)
        assertTrue((blocks[1] as MarkdownBlock.CodeBlock).code.contains("val x = 10"))

        assertTrue(blocks[2] is MarkdownBlock.Divider)
    }

    @Test
    fun testBuildMarkdownAnnotatedString() {
        val text = "تغییرات: **بولد** و *ایتالیک* و `کد` و [لینک](https://cheghad.ir) و ~~خط‌خورده~~"
        val primary = Color.Blue
        val link = Color.Cyan
        val codeBg = Color.LightGray
        val codeText = Color.Black

        val annotated = buildMarkdownAnnotatedString(
            text = text,
            primaryColor = primary,
            linkColor = link,
            codeBgColor = codeBg,
            codeTextColor = codeText
        )

        // Text should be stripped of syntax markers in rendered text
        val raw = annotated.text
        assertTrue(raw.contains("بولد"))
        assertTrue(raw.contains("ایتالیک"))
        assertTrue(raw.contains("کد"))
        assertTrue(raw.contains("لینک"))
        assertTrue(raw.contains("خط‌خورده"))
        assertFalse(raw.contains("**بولد**"))
        assertFalse(raw.contains("~~خط‌خورده~~"))

        // Check styles
        val styles = annotated.spanStyles
        assertTrue(styles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(styles.any { it.item.fontStyle == FontStyle.Italic })
        assertTrue(styles.any { it.item.textDecoration == TextDecoration.LineThrough })
    }

    @Test
    fun testEmptyAndMalformedMarkdown() {
        val emptyBlocks = parseMarkdownBlocks("")
        assertEquals(0, emptyBlocks.size)

        val unclosedBlocks = parseMarkdownBlocks("```\nunclosed code")
        assertEquals(1, unclosedBlocks.size)
        assertTrue(unclosedBlocks[0] is MarkdownBlock.CodeBlock)

        val annotatedUnmatched = buildMarkdownAnnotatedString(
            text = "Unclosed **bold and unclosed [link",
            primaryColor = Color.Blue,
            linkColor = Color.Cyan,
            codeBgColor = Color.Gray,
            codeTextColor = Color.Black
        )
        // Should not crash and should preserve text
        assertTrue(annotatedUnmatched.text.isNotEmpty())
    }
}
