package me.tomasan7.opinet.isolationlevels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.window.singleWindowApplication
import me.tomasan7.opinet.config.FileConfigProvider
import me.tomasan7.opinet.ui.theme.AppTheme
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class IsolationLevels(private val isolationLevel: Int)
{
    private val config = FileConfigProvider("opinet.conf").getConfig()
    private val accountName = "Bob"
    private val database = config.database.let { dbConf ->
        Database.connect(
            url = dbConf.url,
            driver = dbConf.driver,
            user = dbConf.user,
            password = dbConf.password.value,
        )
    }

    init
    {
        transaction(database) {
            SchemaUtils.drop(AccountTable)
            SchemaUtils.create(AccountTable)
            transaction(database) {
                AccountTable.insert {
                    it[name] = accountName
                    it[balance] = 100f
                }
            }
        }
    }

    fun start() = singleWindowApplication(
        title = "IsolationLevels"
    ) {
        AppTheme {
            val model = remember { DirtyReadsModel(database, isolationLevel, accountName) }
            val uiState = model.uiState

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Write transaction",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        TransactionInProgress(uiState.writeTransactionInProgress)
                        Button(
                            enabled = !uiState.writeTransactionInProgress,
                            onClick = { model.startWriteTransaction() }
                        ) {
                            Text("Start write transaction")
                        }
                        Row {
                            TextField(
                                value = uiState.balanceAddition.toString(),
                                singleLine = true,
                                label = { Text("Update value") },
                                onValueChange = { model.setBalanceAddition(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(150.dp)
                                    .onKeyEvent { event ->
                                        if (event.key == Key.Enter)
                                        {
                                            model.writeBalance()
                                            true
                                        }

                                        false
                                    }
                            )
                            Button(
                                enabled = uiState.writeTransactionInProgress,
                                onClick = { model.writeBalance() }
                            ) {
                                Text("Write")
                            }
                        }
                        Row {
                            Button(
                                enabled = uiState.writeTransactionInProgress,
                                onClick = { model.commitWrite() }
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Commit")
                                Text("Commit")
                            }
                            Button(
                                enabled = uiState.writeTransactionInProgress,
                                onClick = { model.rollbackWrite() }
                            ) {
                                Icon(Icons.AutoMirrored.Default.Undo, contentDescription = "Rollback")
                                Text("Rollback")
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
