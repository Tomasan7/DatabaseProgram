package me.tomasan7.opinet.registerscreen

import me.tomasan7.opinet.user.Gender

data class RegisterScreenState(
    val maxLengths: MaxLengths,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val confirmingPassword: String = "",
    val passwordShown: Boolean = false,
    val confirmingPasswordShown: Boolean = false,
    val gender: Gender = Gender.MALE,
    val errorText: String = "",
    val registrationSuccessEvent: Boolean = false
)
{
    data class MaxLengths(
        val username: Int,
        val firstName: Int,
        val lastName: Int,
        val password: Int
    )
}
