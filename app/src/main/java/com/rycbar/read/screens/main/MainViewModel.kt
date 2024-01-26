package com.rycbar.read.screens.main

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rycbar.read.screens.main.mvi.MainEvent
import com.rycbar.read.screens.main.mvi.MainState
import com.rycbar.read.screens.main.mvi.ReadPosition
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
        MainState(
            paragraphs = emptyList(),
            currentReadPosition = ReadPosition.NotStarted,
            editMode = true
        )
    )
    val stateFlow: StateFlow<MainState> = _stateFlow

    init {
        viewModelScope.launch {
            speechRepoDeferred.await()
            speechRepo.stateFlow.collect { speechState ->
                collectFinishedJobs(speechState)
            }
        }
        viewModelScope.launch {
            speechRepo.errorFlow.collect { job ->
                val position = sentenceSplitter.parseFinishedJobPosition(job)
                _stateFlow.update { state ->
                    state.copy(currentReadPosition = position.getAsUiPosition(true))
                }
            }
        }
    }

    fun dispatchEvent(event: MainEvent) {
        when (event) {
            is MainEvent.OnNewText -> viewModelScope.launch { onNewText(event.text) }
            is MainEvent.Arguments -> onCreated(event)
            MainEvent.OnPause -> speechRepo.pause()
            MainEvent.OnResume -> speechRepo.resume()
            MainEvent.OnAddClicked -> _stateFlow.update { state -> state.copy(editMode = true) }
            is MainEvent.OnParagraphClicked -> onParagraphClicked(event)
        }
    }

    private fun onCreated(event: MainEvent.Arguments) {
        val externalText = event.bundle.getString(Intent.EXTRA_PROCESS_TEXT)
        if (externalText != null) {
            _stateFlow.update { state -> state.copy(editMode = false) }
            onNewText(externalText)
        }
    }

    private fun onParagraphClicked(event: MainEvent.OnParagraphClicked) {
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
            state.copy(currentReadPosition = position.getAsUiPosition(false))
        }
    }

    private fun SentenceSplitter.UtterancePosition.getAsUiPosition(asError: Boolean) = ReadPosition.Position(
        paragraph = paragraphIndex,
        charSpan = range,
        isError = asError,
    )
}

