package me.tomasan7.opinet.registerscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.OpiNet
import me.tomasan7.opinet.user.*
import me.tomasan7.opinet.util.size
import me.tomasan7.opinet.util.trimAndCut
import java.nio.channels.UnresolvedAddressException

private val logger = KotlinLogging.logger {}

class RegisterScreenModel(
    username: String = "",
    password: String = "",
    private val userService: UserService,
    private val opiNet: OpiNet
) : ScreenModel
{
    var uiState by mutableStateOf(RegisterScreenState(
        // TODO: ScreenModel should not directly depend on Model implementation. Move this logic to abstract service.
        maxLengths = RegisterScreenState.MaxLengths(
            username = UserTable.username.size,
            firstName = UserTable.firstName.size,
            lastName = UserTable.lastName.size,
            password = 50
        ),
        username = username,
        password = password
    ))
        private set

    fun setUsername(username: String) = changeUiState(username = username.trimAndCut(uiState.maxLengths.username), errorText = "")

    fun setFirstName(firstName: String) = changeUiState(firstName = firstName.trimAndCut(uiState.maxLengths.firstName), errorText = "")

    fun setLastName(lastName: String) = changeUiState(lastName = lastName.trimAndCut(uiState.maxLengths.lastName), errorText = "")

    fun setPassword(password: String) = changeUiState(password = password.trimAndCut(uiState.maxLengths.password), errorText = "")

    fun setConfirmingPassword(confirmingPassword: String) = changeUiState(confirmingPassword = confirmingPassword.trimAndCut(uiState.maxLengths.password), errorText = "")

    fun changePasswordVisibility() = changeUiState(passwordShown = !uiState.passwordShown)

    fun changeConfirmingPasswordVisibility() = changeUiState(confirmingPasswordShown = !uiState.confirmingPasswordShown)

    fun setGender(gender: Gender) = changeUiState(gender = gender)

    fun registrationSuccessEventConsumed() = changeUiState(registrationSuccessEvent = false, errorText = "")

    fun register()
    {
        if (uiState.username.isBlank()
            || uiState.firstName.isBlank()
            || uiState.lastName.isBlank()
            || uiState.password.isBlank()
            || uiState.confirmingPassword.isBlank()
        )
        {
            changeUiState(errorText = "All fields must be filled")
            return
        }

        if (uiState.password != uiState.confirmingPassword)
        {
            changeUiState(errorText = "Passwords do not match")
            return
        }

        val userDto = UserDto(
            username = uiState.username,
            firstName = uiState.firstName,
            lastName = uiState.lastName,
            gender = uiState.gender
        )
        val password = uiState.password

        screenModelScope.launch {
            try
            {
                val newUserId = userService.createUser(userDto, password)
                opiNet.currentUser = userDto.copy(id = newUserId)
                changeUiState(registrationSuccessEvent = true)
            }
            catch (e: UnresolvedAddressException)
            {
                changeUiState(errorText = Messages.networkError)
            }
            catch (e: UsernameAlreadyExistsException)
            {
                changeUiState(errorText = "Username ${uiState.username} already exists")
                logger.error { e.message }
            }
            catch (e: Exception)
            {
                changeUiState(errorText = "There was an unknown error")
                e.printStackTrace()
            }
        }
    }

    private fun changeUiState(
        username: String = uiState.username,
        firstName: String = uiState.firstName,
        lastName: String = uiState.lastName,
        password: String = uiState.password,
        confirmingPassword: String = uiState.confirmingPassword,
        passwordShown: Boolean = uiState.passwordShown,
        confirmingPasswordShown: Boolean = uiState.confirmingPasswordShown,
        gender: Gender = uiState.gender,
        errorText: String = uiState.errorText,
        registrationSuccessEvent: Boolean = uiState.registrationSuccessEvent,
    )
    {
        uiState = uiState.copy(
            username = username,
            firstName = firstName,
            lastName = lastName,
            password = password,
            confirmingPassword = confirmingPassword,
            passwordShown = passwordShown,
            confirmingPasswordShown = confirmingPasswordShown,
            gender = gender,
            errorText = errorText,
            registrationSuccessEvent = registrationSuccessEvent,
        )
    }
    private fun String.removeWhitespace() = this.replace(Regex("\\s"), "")
}
