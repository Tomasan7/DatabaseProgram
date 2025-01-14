package me.tomasan7.opinet.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.SQLServerDialect
import org.jetbrains.exposed.sql.vendors.currentDialect

open class View(name: String, private val select: Query): Table(name)
{
    fun <T> column(originalColumn: Column<T>) = registerColumn<T>(originalColumn.name, originalColumn.columnType)

    override fun createStatement(): List<String>
    {
        val keyword = if (currentDialect is SQLServerDialect) "ALTER" else "REPLACE"

        return listOf("CREATE OR $keyword VIEW $tableName AS ${select.prepareSQL(QueryBuilder(false))}")
    }
}
