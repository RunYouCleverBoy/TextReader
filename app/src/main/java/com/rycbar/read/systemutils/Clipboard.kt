package com.rycbar.read.systemutils

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Clipboard @Inject constructor(@ApplicationContext context: Context) {
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

    fun paste(): CharSequence? {
        return clipboardManager.takeIf {
            it.hasPrimaryClip() && it.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == true
        }?.primaryClip?.getItemAt(0)?.text
    }
}