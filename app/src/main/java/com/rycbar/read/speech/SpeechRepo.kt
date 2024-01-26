package com.rycbar.read.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.rycbar.read.models.UtteranceJob
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class SpeechRepo @Inject constructor(@ApplicationContext val context: Context) {
    private var tts: TextToSpeech? = null
    private val pendingTexts = ConcurrentLinkedQueue<UtteranceJob>()
    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    private val _errorFlow = MutableSharedFlow<UtteranceJob>()
    val errorFlow: SharedFlow<UtteranceJob> = _errorFlow

    data class State (
        val currentJob: UtteranceJob? = null,
        val ready: Boolean = false,
    )

    fun pause() {
        tts?.takeIf { it.isSpeaking }?.stop()
    }

    fun resume() {
        pendingTexts.forEach { (id, text) ->
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, id)
        }
    }

    suspend fun init(): Boolean {
        tts?.takeIf { it.isSpeaking }?.stop()
        tts?.shutdown()
        val readyDeferred = CompletableDeferred<Boolean>()
        val tts = TextToSpeech(context) { status ->
            readyDeferred.complete(status == TextToSpeech.SUCCESS)
        }.also { tts = it }
        val ready = readyDeferred.await()
        _stateFlow.update { state -> state.copy(ready = ready) }

        if (!ready) {
            tts.shutdown()
            this.tts = null
            return false
        }

        tts.language = pickLocale(
            tts.availableLanguages,
            Locale.getDefault(),
            Locale("he", "IL"),
            Locale.US,
            Locale.ENGLISH
        )

        tts.setOnUtteranceProgressListener(UtteranceProgressHandler())
        return true
    }

    fun spoolJobs(jobs: List<UtteranceJob>) {
        pendingTexts.addAll(jobs)
        Log.v(TAG, "spoolJobs: Spooling ${jobs.size} jobs")
        jobs.forEach { (id, text) ->
            val code = tts?.speak(text, TextToSpeech.QUEUE_ADD, null, id)
            Log.v(TAG, "code: $code, id: $id, text: $text")
        }
    }

    fun stop() {
        tts?.stop()
    }

    private fun pickLocale(availableLanguages: Set<Locale>, vararg byPriority: Locale): Locale {
        val availableLocales: Set<Locale> = Locale.getAvailableLocales()
            .intersect(availableLanguages)
            .intersect(byPriority.toSet())

        return byPriority.first{ it in availableLocales }
    }

    private fun consumeUntil(utteranceId: String?): UtteranceJob? {
        var job: UtteranceJob?
        do {
            job = pendingTexts.poll()
        } while (job?.id != utteranceId)
        return job
    }

    private inner class UtteranceProgressHandler : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            Log.v(PROGRESS_TAG, "onStart: $utteranceId")
            _stateFlow.update { state ->
                state.copy(currentJob = pendingTexts.find { it.id == utteranceId })
            }
        }

        override fun onDone(utteranceId: String?) {
            Log.v(PROGRESS_TAG, "onDone: $utteranceId")
            val job: UtteranceJob? = consumeUntil(utteranceId)
            job?.let {
                _stateFlow.update { state -> state.copy(currentJob = null) }
            }
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onError(utteranceId: String?) {
            Log.v(PROGRESS_TAG, "onError: $utteranceId")
            handleError(utteranceId)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            Log.v(PROGRESS_TAG, "onError: $utteranceId, errorCode: $errorCode")
            handleError(utteranceId)
        }

        private fun handleError(utteranceId: String?) {
            val job = consumeUntil(utteranceId)
            if (job == null) {
                _stateFlow.update { state -> state.copy(currentJob = null) }
            }
            job?.let { _errorFlow.tryEmit(it) }
        }
    }

    companion object {
        private const val PROGRESS_TAG = "UtteranceProgressHandler"
        private const val TAG = "SpeechRepo"
    }
}