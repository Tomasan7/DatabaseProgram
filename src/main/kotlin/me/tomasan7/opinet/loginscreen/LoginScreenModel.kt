package me.tomasan7.opinet.loginscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.OpiNet
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.user.UserTable
import me.tomasan7.opinet.util.isNetworkError
import me.tomasan7.opinet.util.size
import me.tomasan7.opinet.util.trimAndCut
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

class LoginScreenModel(
    private val userService: UserService,
    private val opiNet: OpiNet,
    private val sessionFile: Path
) : ScreenModel
{
    var uiState by mutableStateOf(LoginScreenState(
        // TODO: ScreenModel should not directly depend on Model implementation. Move this logic to abstract service.
        maxLengths = LoginScreenState.MaxLengths(
            username = UserTable.username.size
        )
    ))
        private set

    private var loginJob: Job? = null

    init
    {
        screenModelScope.launch {
            val credentials = loadCredentials()

            if (credentials != null)
            {
                val (username, password) = credentials
                changeUiState(username = username, password = password, rememberMe = true)
                login()
            }
        }
    }

    fun setUsername(username: String) = changeUiState(username = username.trimAndCut(uiState.maxLengths.username), errorText = null)

    fun setPassword(password: String) = changeUiState(password = password.trim(), errorText = null)

    fun changePasswordVisibility() = changeUiState(passwordShown = !uiState.passwordShown)

    fun setRememberMe(value: Boolean) = changeUiState(rememberMe = value)

    fun loginSuccessEventConsumed() = changeUiState(loginSuccessEvent = false, errorText = null)

    fun login()
    {
        loginJob?.cancel()

        if (uiState.username.isBlank())
        {
            changeUiState(errorText = "Username cannot be blank")
            return
        }
        else if (uiState.password.isBlank())
        {
            changeUiState(errorText = "Password cannot be blank")
            return
        }

        loginJob = screenModelScope.launch {
            val username = uiState.username
            val password = uiState.password

            if (username.isBlank() || password.isBlank())
                return@launch

            try
            {
                val success = userService.loginUser(username, password)

                if (success)
                {
                    if (uiState.rememberMe)
                        saveCredentials()
                    opiNet.currentUser = userService.getUserByUsername(username)!!
                    uiState = LoginScreenState(
                        maxLengths = uiState.maxLengths,
                        loginSuccessEvent = true
                    )
                }
                else
                {
                    changeUiState(errorText = "Incorrect username or password")
                }
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                {
                    changeUiState(errorText = Messages.networkError)
                }
                else
                {
                    changeUiState(errorText = "There was an unknown error")
                    e.printStackTrace()
                }
            }
        }
    }

    fun errorTextConsumed()
    {
        changeUiState(errorText = null)
    }

    private fun saveCredentials()
    {
        val username = uiState.username
        val password = uiState.password

        val encoded = encode("$username\n$password")

        sessionFile.toFile().writeText(encoded)
    }

    private fun loadCredentials(): Pair<String, String>?
    {
        if (!sessionFile.toFile().exists())
            return null

        val lines = sessionFile.toFile().readLines()

        if (lines.size != 1)
            return null

        val encoded = lines[0]
        val decoded = decode(encoded)
        val (username, password) = decoded.split("\n")

        return username to password
    }

    private fun encode(value: String): String
    {
        val bytes = value.toByteArray(Charsets.UTF_8)
        return bytes.map { (it - 10).toByte() }.toByteArray().decodeToString()
    }

    private fun decode(value: String): String
    {
        val bytes = value.toByteArray(Charsets.UTF_8)
        return bytes.map { (it + 10).toByte() }.toByteArray().decodeToString()
    }

    private fun changeUiState(
        username: String = uiState.username,
        firstName: String = uiState.firstName,
        lastName: String = uiState.lastName,
        password: String = uiState.password,
        passwordShown: Boolean = uiState.passwordShown,
        rememberMe: Boolean = uiState.rememberMe,
        errorText: String? = uiState.errorText,
        loginSuccessEvent: Boolean = uiState.loginSuccessEvent
    )
    {
        uiState = uiState.copy(
            username = username,
            firstName = firstName,
            lastName = lastName,
            password = password,
            passwordShown = passwordShown,
            rememberMe = rememberMe,
            errorText = errorText,
            loginSuccessEvent = loginSuccessEvent
        )
    }
}
