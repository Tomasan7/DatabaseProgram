package me.tomasan7.opinet.loginscreen

data class LoginScreenState(
    val maxLengths: MaxLengths,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val passwordShown: Boolean = false,
    val rememberMe: Boolean = false,
    val errorText: String? = null,
    val loginSuccessEvent: Boolean = false,
) {
    data class MaxLengths(
        val username: Int
    )
}
