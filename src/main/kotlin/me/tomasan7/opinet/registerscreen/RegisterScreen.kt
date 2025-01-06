package me.tomasan7.opinet.registerscreen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alexfacciorusso.previewer.PreviewTheme
import me.tomasan7.opinet.feedscreen.FeedScreen
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.loginscreen.LoginScreen
import me.tomasan7.opinet.ui.component.DropDownSelector
import me.tomasan7.opinet.ui.component.FilledDropDownSelector
import me.tomasan7.opinet.ui.component.PasswordTextField
import me.tomasan7.opinet.ui.component.VerticalSpacer
import me.tomasan7.opinet.user.Gender
import me.tomasan7.opinet.util.AppThemePreviewer


data class RegisterScreen(
    private val username: String = "",
    private val password: String = "",
) : Screen
{
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val opiNet = navigator.getOpiNet()
        val model = rememberScreenModel {
            RegisterScreenModel(
                username,
                password,
                opiNet.userService,
                opiNet
            )
        }
        val uiState = model.uiState

        if (uiState.registrationSuccessEvent)
        {
            model.registrationSuccessEventConsumed()
            navigator push FeedScreen
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            VerticalSpacer(16.dp)
            TextField(
                value = uiState.username,
                singleLine = true,
                onValueChange = { model.setUsername(it) },
                label = { Text("Username") }
            )
            TextField(
                value = uiState.firstName,
                singleLine = true,
                onValueChange = { model.setFirstName(it) },
                label = { Text("First name") }
            )
            TextField(
                value = uiState.lastName,
                singleLine = true,
                onValueChange = { model.setLastName(it) },
                label = { Text("Last name") }
            )

            FilledDropDownSelector(
                items = Gender.entries,
                selectedItem = uiState.gender,
                itemStringMap = { it.toString().replace("_", "-").lowercase().replaceFirstChar { it.uppercase() } },
                onChange = { model.setGender(it) }
            )

            PasswordTextField(
                password = uiState.password,
                onPasswordChange = { model.setPassword(it) },
                onChangeVisibilityClick = { model.changePasswordVisibility() },
                passwordShown = uiState.passwordShown,
                label = { Text("Password") }
            )
            PasswordTextField(
                password = uiState.confirmingPassword,
                onPasswordChange = { model.setConfirmingPassword(it) },
                onChangeVisibilityClick = { model.changeConfirmingPasswordVisibility() },
                passwordShown = uiState.confirmingPasswordShown,
                label = { Text("Confirm password") }
            )
            Text(
                text = uiState.errorText,
                color = MaterialTheme.colorScheme.error
            )
            Button({ model.register() }) {
                Text("Register")
            }
            TextButton({ navigator push LoginScreen }) {
                Text(
                    text = "Go back to login",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
@Preview
fun RegisterScreenPreview()
{
    AppThemePreviewer {
        preview(previewTheme = PreviewTheme.Dark) {
            RegisterScreen().Content()
        }
    }
}
