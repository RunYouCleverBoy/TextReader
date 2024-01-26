package com.rycbar.read.screens.main

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rycbar.read.screens.main.mvi.MainEvent
import com.rycbar.read.screens.main.mvi.MainState
import com.rycbar.read.screens.main.mvi.ReadPosition
import com.rycbar.read.speech.SentenceSplitter
import com.rycbar.read.speech.SpeechRepo
import com.rycbar.read.systemutils.Clipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val speechRepo: SpeechRepo,
    val sentenceSplitter: SentenceSplitter,
    val clipboard: Clipboard
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
            MainEvent.OnPaste -> onPaste()
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

    private fun onPaste() {
        val text = clipboard.paste()?.takeIf { it.isNotBlank() }?.toString() ?: return
        _stateFlow.update { state -> state.copy(editMode = false) }
        onNewText(text)
    }

    private fun onParagraphClicked(event: MainEvent.OnParagraphClicked) {
        speechRepo.stop()
        speechRepo.spoolJobs(
            sentenceSplitter.paragraphsToJobs(
                _stateFlow.value.paragraphs.subList(
                    event.index,
                    _stateFlow.value.paragraphs.size
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
            val paragraphs = withContext(Dispatchers.IO){
                speechRepo.stop()
                sentenceSplitter.textToParagraphs(text)
            }

            _stateFlow.update { state ->
                state.copy(
                    paragraphs = paragraphs,
                    editMode = false,
                    currentReadPosition = ReadPosition.NotStarted
                )
            }

            val jobs = withContext(Dispatchers.IO) {
                sentenceSplitter.paragraphsToJobs(paragraphs)
            }
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

