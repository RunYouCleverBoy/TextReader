package com.rycbar.read.screens.main.mvi

import android.os.Bundle

sealed class MainEvent {
    data object OnPause : MainEvent()
    data object OnResume : MainEvent()
    data object OnAddClicked : MainEvent()
    data object OnPaste : MainEvent()
    data class OnNewText(val text: String) : MainEvent()
    data class Arguments(val bundle: Bundle) : MainEvent()
    data class OnParagraphClicked(val index: Int) : MainEvent()
}