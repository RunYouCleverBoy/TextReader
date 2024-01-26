package com.rycbar.read.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rycbar.read.speech.SentenceSplitter
import com.rycbar.read.speech.SpeechRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val speechRepo: SpeechRepo,
    val sentenceSplitter: SentenceSplitter
) : ViewModel() {
    private val speechRepoDeferred: Deferred<Boolean> = viewModelScope.async {
        speechRepo.init()
    }
    private val _stateFlow = MutableStateFlow(
        State(
            paragraphs = emptyList(),
            currentReadPosition = ReadPosition.NotStarted,
            editMode = true
        )
    )
    val stateFlow: StateFlow<State> = _stateFlow

    init {
        viewModelScope.launch {
            speechRepoDeferred.await()
            speechRepo.stateFlow.collect { speechState ->
                collectFinishedJobs(speechState)
            }
        }
    }

    fun dispatchEvent(event: Event) {
        when (event) {
            is Event.OnNewText -> viewModelScope.launch { onNewText(event.text) }
            Event.OnPause -> speechRepo.pause()
            Event.OnResume -> speechRepo.resume()
            Event.OnAddClicked -> _stateFlow.update { state -> state.copy(editMode = true) }
            is Event.OnParagraphClicked -> {
                speechRepo.stop()
                speechRepo.spoolJobs(
                    sentenceSplitter.paragraphsToJobs(
                        _stateFlow.value.paragraphs.subList(
                            event.index,
                            _stateFlow.value.paragraphs.lastIndex
                        )
                    )
                )
            }
        }
    }

    private fun onNewText(text: String) {
        viewModelScope.launch {
            val ready = speechRepoDeferred.await()
            if (!ready) {
                return@launch
            }
            speechRepo.stop()
            val paragraphs = sentenceSplitter.textToParagraphs(text)
            _stateFlow.update { state ->
                state.copy(
                    paragraphs = paragraphs,
                    editMode = false,
                    currentReadPosition = ReadPosition.NotStarted
                )
            }

            val jobs = sentenceSplitter.paragraphsToJobs(paragraphs)
            speechRepo.spoolJobs(jobs)
        }
    }

    private fun collectFinishedJobs(speechState: SpeechRepo.State) {
        val job = speechState.currentJob ?: return
        val position = sentenceSplitter.parseFinishedJobPosition(job)
        _stateFlow.update { state ->
            state.copy(
                currentReadPosition = ReadPosition.Position(
                    paragraph = position.paragraphIndex,
                    charSpan = position.range
                )
            )
        }
    }
}

sealed class Event {
    data object OnPause : Event()
    data object OnResume : Event()
    data object OnAddClicked : Event()
    data class OnNewText(val text: String) : Event()
    data class OnParagraphClicked(val index: Int) : Event()
}

sealed class ReadPosition {
    class Position(val paragraph: Int, val charSpan: IntRange) : ReadPosition()
    data object NotStarted : ReadPosition()
}

data class State(
    val paragraphs: List<String>,
    val currentReadPosition: ReadPosition,
    val editMode: Boolean
)