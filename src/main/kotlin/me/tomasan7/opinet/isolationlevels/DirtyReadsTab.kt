package me.tomasan7.opinet.isolationlevels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class DirtyReadsTab(
    private val accountName: String
) : Tab
{
    override val options: TabOptions
        @Composable
        get()
        {
            return remember {
                TabOptions(
                    title = "Dirty reads",
                    index = 0u
                )
            }
        }

    @Composable
    override fun Content()
    {
        val model = rememberScreenModel { DirtyReadsModel(accountName) }
        val uiState = model.uiState

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Write transaction",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    TransactionInProgress(uiState.writeTransactionInProgress)
                    if (!uiState.writeTransactionInProgress)
                        Button(
                            onClick = { model.startWriteTransaction() }
                        ) {
                            Text("Start write transaction")
                        }
                    else
                    {
                        Row {
                            TextField(
                                value = uiState.balanceAddition.toString(),
                                singleLine = true,
                                label = { Text("Update value") },
                                onValueChange = { model.setBalanceAddition(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = { model.writeBalance() }
                                ),
                                modifier = Modifier
                                    .width(150.dp)
                            )
                            Button(
                                onClick = { model.writeBalance() }
                            ) {
                                Text("Write")
                            }
                        }
                        Row {
                            Button(
                                onClick = { model.rollbackWrite() },
                            ) {
                                Icon(Icons.AutoMirrored.Default.Undo, contentDescription = "Rollback")
                                Text("Rollback")
                            }
                            Button(
                                onClick = { model.commitWrite() }
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Commit")
                                Text("Commit")
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Read transaction",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    TransactionInProgress(uiState.readTransactionInProgress)
                    Text("Balance: ${uiState.readBalance}")
                    Button(
                        onClick = { model.readBalance() }
                    ) {
                        Text("Read")
                    }
                }
            }
            Column(
                modifier = Modifier
                    .height(200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Action history:",
                    style = MaterialTheme.typography.headlineSmall
                )
                uiState.actionHistory.fastForEachReversed {
                    Text("- $it")
                }
            }
        }
    }

    @Composable
    private fun TransactionInProgress(inProgress: Boolean)
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Transaction in progress:")
            Canvas(Modifier.size(10.dp)) {
                drawCircle(
                    color = if (inProgress) Color.Green else Color.Red
                )
            }
        }
    }
}
