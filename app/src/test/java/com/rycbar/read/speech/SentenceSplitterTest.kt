package com.rycbar.read.speech

import com.rycbar.read.models.UtteranceJob
import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceSplitterTest {
    private val sentenceSplitter = SentenceSplitter()

    @Test
    fun parseFinishedJobPosition() {
        val job = UtteranceJob(
            "{\"paragraphIndex\":0,\"range\":{\"start\":0,\"endInclusive\":5}}",
            "Hello"
        )
        val position = sentenceSplitter.parseFinishedJobPosition(job)
        assertEquals(0, position.paragraphIndex)
        assertEquals(0, position.range.start)
        assertEquals(5, position.range.endInclusive)
    }

    @Test
    fun paragraphsToJobs() {
        val jobs = sentenceSplitter.paragraphsToJobs(listOf(
            "Lorem Ipsum said the opossum. Opossums are marsupials. 123. F. **.",
            "a ",
            ".",
            "'",
            "",
            "  ",
            "Living thing is a song by ELO. ELO is a band. "
        ))
        assertEquals(4 + 1 + 2, jobs.size)
    }

    @Test
    fun splitToRanges() {
        val paragraph = "Hello. Darkness my old friend. "
        val ranges = sentenceSplitter.splitToRanges(paragraph)
        assertEquals(3, ranges.size)
        assertEquals(0..5, ranges[0].first)
        assertEquals("Hello.", ranges[0].second)
        assertEquals(6..paragraph.indexOfLast { it == '.' }, ranges[1].first)
        assertEquals(" Darkness my old friend.", ranges[1].second)
        assertEquals(" ", ranges[2].second)

        val rangesForEmpty = sentenceSplitter.splitToRanges("")
        assertEquals(0, rangesForEmpty.size)

        val rangesForBlank = sentenceSplitter.splitToRanges("  ")
        assertEquals(1, rangesForBlank.size)
        assertEquals("  ", rangesForBlank[0].second)
        assertEquals(0..1, rangesForBlank[0].first)
    }

    @Test
    fun textToParagraphs() {
        val paragraphs = sentenceSplitter.textToParagraphs("Hello\nDarkness my old friend\n\n")
        assertEquals(4, paragraphs.size)
    }
}