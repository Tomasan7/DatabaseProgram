package me.tomasan7.opinet.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val username: String,
    val firstName: String,
    val lastName: String,
    val gender: Gender,
    val id: Int? = null
)
