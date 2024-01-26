package com.rycbar.read.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.rycbar.read.screens.main.ReadPosition
import com.rycbar.read.screens.main.State

@Composable
fun ParagraphText(state: State, index: Int, onClick: () -> Unit) {
    val position =
        (state.currentReadPosition as? ReadPosition.Position)?.takeIf { index == it.paragraph }
    val span = position?.charSpan?.takeIf { !it.isEmpty() }
    if (span != null) {
        val text = state.paragraphs[index]
        val before = text.substring(0, span.first)
        val highlighted = text.substring(span.first, span.last + 1)
        val after = text.substring(span.last + 1)
        val styledText = buildAnnotatedString {
            append(before)
            withStyle(SpanStyle(background = Color.Yellow.copy(alpha = 0.5f))) {
                append(highlighted)
            }
            append(after)
        }
        Text(text = styledText, modifier = Modifier.clickable(onClick = onClick))
    } else {
        Text(text = state.paragraphs[index], modifier = Modifier.clickable(onClick = onClick))
    }
}