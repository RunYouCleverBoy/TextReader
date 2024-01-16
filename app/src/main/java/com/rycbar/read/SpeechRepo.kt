package com.rycbar.read

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import java.util.Locale

class SpeechRepo {
    private data class State(
        val tts: TextToSpeech? = null,
        val readyPromise: CompletableDeferred<Boolean> = CompletableDeferred(
            false
        ),
        val chunkedText: List<String> = listOf()
    )

    private var state: State = State()

    fun pause() {
        state.tts?.takeIf { it.isSpeaking }?.stop()
    }

    fun resume() {
        val tts = state.tts?: return
        state.chunkedText.forEach{  chunk ->
            tts.speak(chunk, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    suspend fun initWith(appContext: Context, paragraphs: List<String>) {
        state.let { state ->
            val tts = state.tts
            tts?.takeIf { it.isSpeaking }?.stop()
            tts?.shutdown()
            state.readyPromise.takeIf { it.isActive }?.cancel()
        }
        state = State()
        val readyPromise: CompletableDeferred<Boolean> = CompletableDeferred()
        val tts = TextToSpeech(appContext) { status ->
            readyPromise.complete(status == TextToSpeech.SUCCESS)
        }
        val ready = readyPromise.await()
        if (ready) {
            Log.v("SpeechRepo", "TTS ready")
            tts.language = Locale.UK
            val chunkedText = paragraphs.flatMap { it.chunked(4000) }
            state = State(tts, readyPromise, chunkedText)
        }
        resume()
    }

}