package com.rycbar.read

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.rycbar.read.ui.theme.ReadNotepadTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModels<MainViewModel>()
        setContent {
            val state by viewModel.stateFlow.collectAsState()
            ReadNotepadTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val enableButtons = !state.editMode
                    Scaffold(topBar = { TopBar(enableButtons, viewModel::dispatchEvent) }) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            OnLifecycleEvent(onEvent = { _, event ->
                                when (event) {
                                    Lifecycle.Event.ON_PAUSE -> viewModel.dispatchEvent(Event.OnPause)
                                    else -> Unit
                                }
                            })
                            var inputText by remember { mutableStateOf("") }
                            if (state.editMode) {
                                TextRow(inputText, onTextChanged = { inputText = it }, onOk = {
                                    viewModel.dispatchEvent(Event.OnNewText(applicationContext, inputText))
                                }, onClear = {
                                    inputText = ""
                                })
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(state.paragraphs.size) { index ->
                                        ParagraphText(state, index) {
                                            viewModel.dispatchEvent(Event.OnParagraphClicked(index))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ParagraphText(state: State, index: Int, onClick: () -> Unit) {
        val position = (state.currentReadPosition as? ReadPosition.Position)?.takeIf { index == it.paragraph }
        val span = position?.charSpan?.takeIf { !it.isEmpty() }
        if (span != null) {
            val text = state.paragraphs[index]
            val before = text.substring(0, span.first)
            val highlighted = text.substring(span.first, span.last + 1)
            val after = text.substring(span.last + 1)
            val styledText = buildAnnotatedString {
                append(before)
                withStyle(SpanStyle(background=Color.Yellow.copy(alpha = 0.5f))) {
                    append(highlighted)
                }
                append(after)
            }
            Text(text = styledText, modifier = Modifier.clickable(onClick = onClick))
        } else {
            Text(text = state.paragraphs[index], modifier = Modifier.clickable(onClick = onClick))
        }
    }

}

@Composable
private fun TextRow(inputText: String, onTextChanged: (String) -> Unit, onOk: (String) -> Unit, onClear: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            value = inputText,
            onValueChange = onTextChanged,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = { onClear() }) {
                Image(
                    colorFilter = ColorFilter.tint(Color.Red),
                    painter = painterResource(id = R.drawable.outline_delete_24),
                    contentDescription = stringResource(id = R.string.ok)
                )
            }
            Spacer(modifier = Modifier.size(24.dp))
            IconButton(onClick = { onOk(inputText) }) {
                Image(
                    colorFilter = ColorFilter.tint(Color.Green),
                    painter = painterResource(id = R.drawable.baseline_check_24),
                    contentDescription = stringResource(id = R.string.ok)
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
    }
}

@Composable
fun TopBar(enableButtons: Boolean, onEvent: (Event) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(16.dp)
    ) {
        Text(text = "Read Notepad", modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(24.dp))
        if (enableButtons) {
            IconButton(onClick = { onEvent(Event.OnAddClicked) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { onEvent(Event.OnPause) }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.add),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { onEvent(Event.OnResume) }) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = R.string.add),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    ReadNotepadTheme {
        TopBar(true) {}
    }
}

@Preview(showBackground = true)
@Composable
fun TextRowPreview() {
    ReadNotepadTheme {
        TextRow(
            "slkjhf sakdfjhskljf slkfh skljfhskjfhs lkfjhslkfjhsakjfhs klfhsldkafj lskadjhfdsaj fklsdh ",
            {},
            {},
            {})
    }
}