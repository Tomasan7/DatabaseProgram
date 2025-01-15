package me.tomasan7.opinet.isolationlevels

import org.jetbrains.exposed.dao.id.IntIdTable

object AccountTable: IntIdTable("account")
{
    val name = varchar("name", 50).uniqueIndex()
    val balance = float("balance")
}
