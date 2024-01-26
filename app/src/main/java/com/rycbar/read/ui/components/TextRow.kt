package com.rycbar.read.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rycbar.read.R
import com.rycbar.read.ui.theme.ReadNotepadTheme

@Composable
fun TextRow(inputText: String, onTextChanged: (String) -> Unit, onOk: (String) -> Unit, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
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
                    contentDescription = stringResource(id = R.string.clear)
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
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