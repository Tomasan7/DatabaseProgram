package me.tomasan7.opinet.isolationlevels

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun UpdateTransaction(
    updateValue: String,
    inProgress: Boolean,
    onUpdateValueChange: (String) -> Unit,
    onStart: () -> Unit,
    onWrite: () -> Unit,
    onCommit: () -> Unit,
    onRollback: () -> Unit
)
{
    if (!inProgress)
        Button(
            onClick = onStart
        ) {
            Text("Start write transaction")
        }
    else
    {
        Row {
            TextField(
                value = updateValue.toString(),
                singleLine = true,
                label = { Text("Update value") },
                onValueChange = onUpdateValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onWrite() }
                ),
                modifier = Modifier
                    .width(150.dp)
            )
            Button(
                onClick = onWrite
            ) {
                Text("Write")
            }
        }
        Row {
            Button(
                onClick = onRollback,
            ) {
                Icon(Icons.AutoMirrored.Default.Undo, contentDescription = "Rollback")
                Text("Rollback")
            }
            Button(
                onClick = onCommit
            ) {
                Icon(Icons.Default.Done, contentDescription = "Commit")
                Text("Commit")
            }
        }
    }
}
