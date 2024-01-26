package com.rycbar.read.screens.main.mvi

sealed class MainEvent {
    data object OnPause : MainEvent()
    data object OnResume : MainEvent()
    data object OnAddClicked : MainEvent()
    data class OnNewText(val text: String) : MainEvent()
    data class OnParagraphClicked(val index: Int) : MainEvent()
}