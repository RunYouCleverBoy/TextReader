package com.rycbar.read.di

import android.content.Context
import com.rycbar.read.speech.SentenceSplitter
import com.rycbar.read.speech.SpeechRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
class ReadModule {
    @Provides
    fun getSpeechRepo(@ApplicationContext context: Context): SpeechRepo {
        return SpeechRepo(context)
    }

    @Provides
    fun getSentenceSplitter(): SentenceSplitter {
        return SentenceSplitter()
    }

    @Provides
    fun getContext(@ApplicationContext context: Context): Context {
        return context
    }
}