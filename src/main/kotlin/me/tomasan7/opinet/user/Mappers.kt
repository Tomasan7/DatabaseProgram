package me.tomasan7.opinet.user

import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toUser() = UserDto(
    username = this[UserTable.username],
    firstName = this[UserTable.firstName],
    lastName = this[UserTable.lastName],
    gender = this[UserTable.gender],
    id = this[UserTable.id].value
)
