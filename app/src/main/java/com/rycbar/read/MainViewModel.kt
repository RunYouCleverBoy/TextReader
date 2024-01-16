package com.rycbar.read

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _stateFlow = MutableStateFlow(State(
        parsed = arrayListOf(),
        currentReadPosition = ReadPosition.NotStarted,
        shouldShowText = true
    ))
    val stateFlow: StateFlow<State> = _stateFlow

    private val speechRepo by lazy { SpeechRepo() }

    fun dispatchEvent(event: Event) {
        when (event) {
            is Event.OnNewText -> viewModelScope.launch { onNewText(event.appContext, event.text) }
            Event.OnPause -> speechRepo.pause()
            Event.OnResume -> speechRepo.resume()
            Event.OnAddClicked -> _stateFlow.update { state -> state.copy(shouldShowText = true) }
        }
    }

    private suspend fun onNewText(appContext: Context, text: String) {
        val paragraphs = withContext(Dispatchers.IO) {
            ArrayList(text.split("\n"))
        }
        _stateFlow.update { state ->
            state.copy(
                parsed = paragraphs,
                shouldShowText = false,
                currentReadPosition = ReadPosition.NotStarted
            )
        }

        speechRepo.initWith(appContext, paragraphs)
    }
}

sealed class Event {
    data object OnPause : Event()
    data object OnResume : Event()
    data object OnAddClicked : Event()
    data class OnNewText(val appContext: Context, val text: String) : Event()
}

sealed class ReadPosition {
    class Position(val row: Int, val column: Int) : ReadPosition()
    data object NotStarted : ReadPosition()
}

data class State(
    val parsed: ArrayList<String>,
    val currentReadPosition: ReadPosition,
    val shouldShowText: Boolean
)