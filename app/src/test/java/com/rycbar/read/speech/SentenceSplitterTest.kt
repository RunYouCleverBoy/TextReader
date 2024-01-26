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
    fun textToParagraphs() {
        val paragraphs = sentenceSplitter.textToParagraphs("Hello\nDarkness my old friend\n\n")
        assertEquals(4, paragraphs.size)
    }
}