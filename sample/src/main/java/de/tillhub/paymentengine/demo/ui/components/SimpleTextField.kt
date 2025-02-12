package de.tillhub.paymentengine.demo.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SimpleTextField(label: String, mutableStateFlow: MutableStateFlow<String>) {
    var text by remember { mutableStateOf(TextFieldValue(mutableStateFlow.value)) }

    TextField(
        value = text,
        onValueChange = { newText: TextFieldValue ->
            text = newText
            mutableStateFlow.value = newText.text
        },
        label = { Text(label) },
        modifier = Modifier.padding(vertical = 3.dp).fillMaxWidth()

    )
}
