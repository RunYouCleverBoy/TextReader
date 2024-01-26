package com.rycbar.read.speech

import com.rycbar.read.models.UtteranceJob
import com.rycbar.read.serializers.IntRangeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SentenceSplitter {
    @Serializable
    data class UtterancePosition(
        val paragraphIndex: Int,
        @Serializable(with = IntRangeSerializer::class) val range: IntRange
    )
    fun parseFinishedJobPosition(job: UtteranceJob): UtterancePosition {
        return Json.decodeFromString(UtterancePosition.serializer(), job.id)
    }

    fun paragraphsToJobs(paragraphs: List<String>): List<UtteranceJob> {
        val jobs = paragraphs.flatMapIndexed { paragraphIndex, paragraph ->
            val sentences = mutableListOf<Pair<IntRange, String>>()
            var sentenceStart = 0
            paragraph.forEachIndexed { i,c ->
                if (c == '.') {
                    val indexRange = sentenceStart..i
                    sentences.add(indexRange to paragraph.substring(indexRange))
                    sentenceStart = i + 1
                }
            }
            sentences.add(sentenceStart..paragraph.length to paragraph.substring(sentenceStart))

            sentences.mapNotNull { (indices, text) ->
                if (text.isBlank() || text.none { it.isLetterOrDigit() }) {
                    return@mapNotNull null
                }
                UtteranceJob(
                    Json.encodeToString(
                        UtterancePosition(
                            paragraphIndex,
                            indices
                        )
                    ),
                    text.trim()
                )
            }
        }
        return jobs
    }

    fun textToParagraphs(text: String) = text.split("\n")
}