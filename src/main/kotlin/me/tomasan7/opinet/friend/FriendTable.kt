package me.tomasan7.opinet.friend

import me.tomasan7.opinet.user.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object FriendTable : Table("friend")
{
    val requesterId = reference("requester_id", UserTable)
    val targetId = reference("target_id", UserTable)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(requesterId, targetId)

    init {
        check { requesterId neq targetId }
    }
}
