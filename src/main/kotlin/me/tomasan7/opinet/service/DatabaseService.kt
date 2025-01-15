package me.tomasan7.opinet.service

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.coroutines.CoroutineContext

open class DatabaseService(
    private val database: Database,
    private vararg val tables: Table,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
)
{
    suspend fun init() = dbQuery {
        SchemaUtils.create(*tables)
    }

    protected suspend fun <T> dbQuery(statement: Transaction.() -> T) =
        newSuspendedTransaction(coroutineContext, database, statement = statement)
}
