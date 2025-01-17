package me.tomasan7.opinet.database

import org.jetbrains.exposed.sql.CharColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.VarCharColumnType

class StringTooLongException(
    val string: String,
    val maxLength: Int,
    val fieldName: String? = null
) : IllegalArgumentException(
    "${fieldName ?: "String"} with value \"$string\" is too long. Max allowed length is $maxLength."
)

infix fun String.checkLength(maxLength: Int)
{
    if (this.length > maxLength)
        throw StringTooLongException(this, maxLength)
}

infix fun String.checkLength(column: Column<String>)
{
    when (column.columnType)
    {
        is CharColumnType -> throw StringTooLongException(this, (column.columnType as CharColumnType).colLength, column.name)
        is VarCharColumnType -> throw StringTooLongException(this, (column.columnType as VarCharColumnType).colLength, column.name)
        else -> throw IllegalArgumentException("Column type ${column::class.simpleName} is not supported")
    }
}
