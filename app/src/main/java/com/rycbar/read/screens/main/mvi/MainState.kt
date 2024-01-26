package com.rycbar.read.screens.main.mvi

data class MainState(
    val paragraphs: List<String>,
    val currentReadPosition: ReadPosition,
    val editMode: Boolean
)

sealed class ReadPosition {
    class Position(val isError: Boolean, val paragraph: Int, val charSpan: IntRange) : ReadPosition()
    data object NotStarted : ReadPosition()
}