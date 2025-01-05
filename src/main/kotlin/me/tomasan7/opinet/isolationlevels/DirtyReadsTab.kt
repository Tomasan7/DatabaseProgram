package me.tomasan7.opinet.isolationlevels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Write transaction",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        TransactionProgressIndicator(uiState.writeTransactionInProgress)
                    }
                    UpdateTransaction(
                        updateValue = uiState.updateValue,
                        inProgress = uiState.writeTransactionInProgress,
                        onUpdateValueChange = { model.setUpdateValue(it) },
                        onStart = { model.startWriteTransaction() },
                        onWrite = { model.writeBalance() },
                        onCommit = { model.commitWrite() },
                        onRollback = { model.rollbackWrite() }
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Read transaction",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        TransactionProgressIndicator(uiState.readTransactionInProgress)
                    }
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
}
