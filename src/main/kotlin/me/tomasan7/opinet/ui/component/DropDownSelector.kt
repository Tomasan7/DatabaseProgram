package me.tomasan7.opinet.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownSelector(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T?,
    itemStringMap: @Composable (T?) -> String = { it.toString() },
    onChange: (T) -> Unit,
    textField: @Composable ExposedDropdownMenuBoxScope.(String, Boolean) -> Unit
)
{
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        textField(itemStringMap(selectedItem), expanded)

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { option ->
                DropdownMenuItem(
                    text = { Text(itemStringMap(option)) },
                    onClick = { expanded = false; onChange(option) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilledDropDownSelector(
    modifier: Modifier = Modifier,
    label: String? = null,
    items: List<T>,
    selectedItem: T?,
    itemStringMap: @Composable (T?) -> String = { it.toString() },
    onChange: (T) -> Unit
)
{
    DropDownSelector(
        modifier = modifier,
        items = items,
        selectedItem = selectedItem,
        itemStringMap = itemStringMap,
        onChange = onChange,
        textField = { value, expanded ->
            TextField(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                label = label?.let { { Text(label) } },
                value = value,
                readOnly = true,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OutlinedDropDownSelector(
    modifier: Modifier = Modifier,
    label: String? = null,
    items: List<T>,
    selectedItem: T?,
    itemStringMap: @Composable (T?) -> String = { it.toString() },
    onChange: (T) -> Unit
)
{
    DropDownSelector(
        modifier = modifier,
        items = items,
        selectedItem = selectedItem,
        itemStringMap = itemStringMap,
        onChange = onChange,
        textField = { value, expanded ->
            OutlinedTextField(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                label = label?.let { { Text(label) } },
                value = value,
                readOnly = true,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
        }
    )
}
