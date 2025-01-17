package me.tomasan7.opinet.util

import org.jetbrains.exposed.sql.CharColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.VarCharColumnType

val Column<String>.size: Int
    get()
    {
        return when (this.columnType)
        {
            is VarCharColumnType -> (this.columnType as VarCharColumnType).colLength
            is CharColumnType -> (this.columnType as CharColumnType).colLength
            else -> throw IllegalArgumentException("Column type ${this.columnType::class.simpleName} is not supported")
        }
    }

