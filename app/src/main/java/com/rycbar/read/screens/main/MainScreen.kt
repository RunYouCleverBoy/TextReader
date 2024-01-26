package com.rycbar.read.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import com.rycbar.read.screens.main.mvi.MainEvent
import com.rycbar.read.screens.main.mvi.MainState
import com.rycbar.read.ui.components.ParagraphText
import com.rycbar.read.ui.components.TextRow
import com.rycbar.read.ui.components.TopBar
import com.rycbar.read.ui.theme.ReadNotepadTheme
import com.rycbar.read.ui.utils.OnLifecycleEvent

@Composable
fun MainScreen(state: MainState, dispatchEvent: (MainEvent) -> Unit) {
    ReadNotepadTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val enableButtons = !state.editMode
            Scaffold(topBar = {
                TopBar(
                    enableButtons,
                    dispatchEvent
                )
            }) { paddingValues ->
                OnLifecycleEvent(eventToHandle = Lifecycle.Event.ON_PAUSE) { _, _ ->
                    dispatchEvent(MainEvent.OnPause)
                }

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    var inputText by remember { mutableStateOf("") }
                    if (state.editMode) {
                        TextRow(inputText, onTextChanged = { inputText = it }, onOk = {
                            dispatchEvent(
                                MainEvent.OnNewText(
                                    inputText
                                )
                            )
                        }, onClear = {
                            inputText = ""
                        })
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.paragraphs.size) { index ->
                                ParagraphText(state, index) {
                                    dispatchEvent(MainEvent.OnParagraphClicked(index))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
