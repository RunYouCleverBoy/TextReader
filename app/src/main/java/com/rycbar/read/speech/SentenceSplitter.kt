package com.rycbar.read.speech

import com.rycbar.read.models.UtteranceJob
import com.rycbar.read.serializers.IntRangeSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SentenceSplitter {
    @Serializable
    data class UtterancePosition(
        val paragraphIndex: Int,
        @Serializable(with = IntRangeSerializer::class) val range: IntRange
    ) {
        companion object {
            val INVALID = UtterancePosition(-1, IntRange.EMPTY)
        }
    }

    private val _stateFlow = MutableStateFlow(UtterancePosition.INVALID)

    fun onJobComplete(job: UtteranceJob) {
        _stateFlow.value = Json.decodeFromString(UtterancePosition.serializer(), job.id)
    }

    val stateFlow: StateFlow<UtterancePosition> = _stateFlow
    fun paragraphsToJobs(paragraphs: List<String>): List<UtteranceJob> {
        val jobs = paragraphs.flatMapIndexed { paragraphIndex, paragraph ->
            val sentences = paragraph.split(".")
            val indicesMap = sentences.fold(mutableListOf<IntRange>()) { acc, sentence ->
                val startIndex = acc.lastOrNull()?.last?.plus(1) ?: 0
                acc.add(IntRange(startIndex, startIndex + sentence.length))
                acc
            }
            sentences.mapIndexed { sentenceIndex, sentence ->
                UtteranceJob(
                    Json.encodeToString(
                        UtterancePosition(
                            paragraphIndex,
                            indicesMap[sentenceIndex]
                        )
                    ),
                    sentence
                )
            }
        }
        return jobs
    }

    fun textToParagraphs(text: String) = text.split("\n")
}