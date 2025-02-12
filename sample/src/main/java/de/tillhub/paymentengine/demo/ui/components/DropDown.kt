package de.tillhub.paymentengine.demo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import de.tillhub.paymentengine.demo.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(listOfItems: List<String>, label: String, onValueChange: (newValue: String) -> Unit) {
    var selectedItem by remember { mutableStateOf(listOfItems[0]) }
    var isExpanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        Modifier.padding(vertical = 3.dp)
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            listOfItems.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        selectedItem = listOfItems[index]
                        isExpanded = false
                        onValueChange(listOfItems[index])
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

