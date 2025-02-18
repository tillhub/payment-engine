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

@Composable
fun SimpleTextField(label: String, init: String?, onChange: (newText:String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue(init.orEmpty())) }

    TextField(
        value = text,
        onValueChange = { newText: TextFieldValue ->
            text = newText
            onChange(newText.text)
        },
        label = { Text(label) },
        modifier = Modifier.padding(vertical = 3.dp).fillMaxWidth()

    )
}
