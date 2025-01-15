package me.tomasan7.opinet.isolationlevels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class DirtyWritesTab(
    private val accountName: String
) : Tab
{
    override val options: TabOptions
        @Composable
        get() {
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Dirty Writes"
                )
            }
        }

    @Composable
    override fun Content()
    {
        val model = rememberScreenModel { DirtyWritesModel(accountName) }
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
                            text = "Write transaction 1",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        TransactionProgressIndicator(uiState.transaction1InProgress)
                    }
                    UpdateTransaction(
                        updateValue = uiState.updateValue1,
                        inProgress = uiState.transaction1InProgress,
                        onUpdateValueChange = { model.setUpdateValue1(it) },
                        onStart = { model.startWrite1Transaction() },
                        onWrite = { model.writeBalance1() },
                        onCommit = { model.commitWrite1() },
                        onRollback = { model.rollbackWrite1() }
                    )
                    if (uiState.expectedValue1 != null)
                        Text("Expected value: ${uiState.expectedValue1}")
                    if (uiState.actualValue1 != null)
                        Text("Actual value: ${uiState.actualValue1}")
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Write transaction 2",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        TransactionProgressIndicator(uiState.transaction2InProgress)
                    }
                    UpdateTransaction(
                        updateValue = uiState.updateValue2,
                        inProgress = uiState.transaction2InProgress,
                        onUpdateValueChange = { model.setUpdateValue2(it) },
                        onStart = { model.startWrite2Transaction() },
                        onWrite = { model.writeBalance2() },
                        onCommit = { model.commitWrite2() },
                        onRollback = { model.rollbackWrite2() }
                    )
                    if (uiState.expectedValue2 != null)
                        Text("Expected value: ${uiState.expectedValue2}")
                    if (uiState.actualValue2 != null)
                        Text("Actual value: ${uiState.actualValue2}")
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
