package me.tomasan7.opinet.isolationlevels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class DirtyReadsModel(
    private val accountName: String
): ScreenModel
{
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var writeTransactionContinuation: Continuation<WriteTransactionAction<Float>>? = null

    var uiState by mutableStateOf(DirtyReadsState())
        private set

    init
    {
        readBalance()
    }


    fun startWriteTransaction()
    {
        uiState = uiState.copy(writeTransactionInProgress = true, actionHistory = uiState.actionHistory + "Write transaction started")
        coroutineScope.launch(Dispatchers.IO) {
            newSuspendedTransaction {
                var action = suspend()
                while (true)
                {
                    val actionVal = action
                    when (actionVal)
                    {
                        is WriteTransactionAction.Write ->
                        {
                            AccountTable.update({ AccountTable.name eq accountName }) {
                                it[AccountTable.balance] = actionVal.value
                            }
                        }

                        is WriteTransactionAction.Commit ->
                        {
                            commit()
                            break
                        }

                        is WriteTransactionAction.Rollback ->
                        {
                            rollback()
                            break
                        }
                    }

                    action = suspend()
                }
            }
        }
    }

    fun readBalance()
    {
        uiState = uiState.copy(readTransactionInProgress = true)
        coroutineScope.launch {
            val balance = withContext(Dispatchers.IO) {
                newSuspendedTransaction {
                    AccountTable.selectAll().where { AccountTable.name eq accountName }.single()[AccountTable.balance]
                }
            }
            uiState = uiState.copy(readBalance = balance, readTransactionInProgress = false, actionHistory = uiState.actionHistory + "Read balance: $balance")
        }
    }

    fun writeBalance()
    {
        writeTransactionContinuation?.resume(
            WriteTransactionAction.Write(
                uiState.balanceAddition.toFloatOrNull() ?: 0f
            )
        )
        uiState = uiState.copy(actionHistory = uiState.actionHistory + "Write balance: ${uiState.balanceAddition}")
    }

    fun commitWrite()
    {
        writeTransactionContinuation?.resume(WriteTransactionAction.Commit())
        uiState = uiState.copy(writeTransactionInProgress = false, actionHistory = uiState.actionHistory + "Write commited")
    }

    fun rollbackWrite()
    {
        writeTransactionContinuation?.resume(WriteTransactionAction.Rollback())
        uiState = uiState.copy(writeTransactionInProgress = false, actionHistory = uiState.actionHistory + "Write rolled back")
    }

    private suspend fun suspend() = suspendCancellableCoroutine {
        writeTransactionContinuation = it
    }

    fun setBalanceAddition(value: String)
    {
        uiState = uiState.copy(balanceAddition = value)
    }
}

private sealed interface WriteTransactionAction<T>
{
    data class Write<T>(val value: T) : WriteTransactionAction<T>
    class Commit<T>() : WriteTransactionAction<T>
    class Rollback<T>() : WriteTransactionAction<T>
}
