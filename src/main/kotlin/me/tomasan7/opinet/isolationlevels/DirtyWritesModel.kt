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

class DirtyWritesModel(
    private val accountName: String
) : ScreenModel
{
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var writeTransaction1Continuation: Continuation<WriteTransactionAction<Float>>? = null
    private var writeTransaction2Continuation: Continuation<WriteTransactionAction<Float>>? = null

    var uiState by mutableStateOf(DirtyWritesState())
        private set

    fun startWrite1Transaction()
    {
        uiState = uiState.copy(
            transaction1InProgress = true,
            actionHistory = uiState.actionHistory + "Write transaction 1 started",
            expectedValue1 = null,
            actualValue1 = null
        )
        coroutineScope.launch(Dispatchers.IO) {
            newSuspendedTransaction {
                var action = suspend1()
                while (true)
                {
                    val actionVal = action
                    when (actionVal)
                    {
                        is WriteTransactionAction.Write ->
                        {
                            AccountTable.update({ AccountTable.name eq accountName }) {
                                it[balance] = actionVal.value
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

                    action = suspend1()
                }
            }
        }
    }

    fun startWrite2Transaction()
    {
        uiState = uiState.copy(
            transaction2InProgress = true,
            actionHistory = uiState.actionHistory + "Write transaction 2 started",
            expectedValue2 = null,
            actualValue2 = null
        )
        coroutineScope.launch(Dispatchers.IO) {
            newSuspendedTransaction {
                var action = suspend2()
                while (true)
                {
                    val actionVal = action
                    when (actionVal)
                    {
                        is WriteTransactionAction.Write ->
                        {
                            AccountTable.update({ AccountTable.name eq accountName }) {
                                it[balance] = actionVal.value
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

                    action = suspend2()
                }
            }
        }
    }

    private suspend fun getActualBalance(): Float
    {
        return withContext(Dispatchers.IO) {
            newSuspendedTransaction {
                AccountTable.selectAll().where { AccountTable.name eq accountName }.single()[AccountTable.balance]
            }
        }
    }

    fun writeBalance1()
    {
        writeTransaction1Continuation?.resume(
            WriteTransactionAction.Write(
                uiState.updateValue1.toFloatOrNull() ?: 0f
            )
        )
        uiState = uiState.copy(actionHistory = uiState.actionHistory + "Write balance 1: ${uiState.updateValue1}")
    }

    fun writeBalance2()
    {
        writeTransaction2Continuation?.resume(
            WriteTransactionAction.Write(
                uiState.updateValue2.toFloatOrNull() ?: 0f
            )
        )
        uiState = uiState.copy(actionHistory = uiState.actionHistory + "Write balance 2: ${uiState.updateValue2}")
    }

    fun commitWrite1()
    {
        writeTransaction1Continuation?.resume(WriteTransactionAction.Commit())
        uiState = uiState.copy(transaction1InProgress = false, actionHistory = uiState.actionHistory + "Write 1 commited")
        showWrite1Result()
    }

    fun showWrite1Result()
    {
        coroutineScope.launch {
            val actualValue = getActualBalance()
            uiState = uiState.copy(actualValue1 = actualValue, expectedValue1 = uiState.updateValue1.toFloatOrNull())
        }
    }

    fun showWrite2Result()
    {
        coroutineScope.launch {
            val actualValue = getActualBalance()
            uiState = uiState.copy(actualValue2 = actualValue, expectedValue2 = uiState.updateValue2.toFloatOrNull())
        }
    }

    fun commitWrite2()
    {
        writeTransaction2Continuation?.resume(WriteTransactionAction.Commit())
        uiState = uiState.copy(transaction2InProgress = false, actionHistory = uiState.actionHistory + "Write 2 commited")
        showWrite2Result()
    }

    fun rollbackWrite1()
    {
        writeTransaction1Continuation?.resume(WriteTransactionAction.Rollback())
        uiState =
            uiState.copy(transaction1InProgress = false, actionHistory = uiState.actionHistory + "Write 1 rolled back")
    }

    fun rollbackWrite2()
    {
        writeTransaction2Continuation?.resume(WriteTransactionAction.Rollback())
        uiState =
            uiState.copy(transaction2InProgress = false, actionHistory = uiState.actionHistory + "Write 2 rolled back")
    }

    private suspend fun suspend1() = suspendCancellableCoroutine {
        writeTransaction1Continuation = it
    }

    private suspend fun suspend2() = suspendCancellableCoroutine {
        writeTransaction2Continuation = it
    }

    fun setUpdateValue1(value: String)
    {
        uiState = uiState.copy(updateValue1 = value)
    }

    fun setUpdateValue2(value: String)
    {
        uiState = uiState.copy(updateValue2 = value)
    }
}

