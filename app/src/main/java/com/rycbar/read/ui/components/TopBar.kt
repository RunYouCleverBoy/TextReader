package com.rycbar.read.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rycbar.read.R
import com.rycbar.read.screens.main.mvi.MainEvent
import com.rycbar.read.ui.theme.ReadNotepadTheme

@Composable
fun TopBar(enableButtons: Boolean, onEvent: (MainEvent) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.top_bar_label), modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(24.dp))
        if (enableButtons) {
            IconButton(onClick = { onEvent(MainEvent.OnAddClicked) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { onEvent(MainEvent.OnPause) }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.add),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { onEvent(MainEvent.OnResume) }) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = R.string.add),
                    modifier = Modifier.size(24.dp)
                )
            }
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