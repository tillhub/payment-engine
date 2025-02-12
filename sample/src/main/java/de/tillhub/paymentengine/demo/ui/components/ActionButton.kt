package de.tillhub.paymentengine.demo.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import de.tillhub.paymentengine.data.TerminalOperationStatus

@Composable
fun Greeting(state: State<TerminalOperationStatus>, modifier: Modifier = Modifier) {
    Text(
        text = state.value.toString(),
        modifier = modifier
    )
}