package me.tomasan7.opinet.loginscreen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alexfacciorusso.previewer.PreviewTheme
import me.tomasan7.opinet.feedscreen.FeedScreen
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.registerscreen.RegisterScreen
import me.tomasan7.opinet.ui.component.PasswordTextField
import me.tomasan7.opinet.ui.component.VerticalSpacer
import me.tomasan7.opinet.util.AppThemePreviewer

object LoginScreen : Screen
{
    private fun readResolve(): Any = LoginScreen

    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val opiNet = navigator.getOpiNet()
        val model = rememberScreenModel { LoginScreenModel(opiNet.userService, opiNet) }
        val uiState = model.uiState

        if (uiState.loginSuccessEvent)
        {
            model.loginSuccessEventConsumed()
            navigator push FeedScreen
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            VerticalSpacer(16.dp)
            TextField(
                value = uiState.username,
                singleLine = true,
                onValueChange = { model.setUsername(it) },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Next
                ),
                label = { Text("Username") }
            )
            PasswordTextField(
                password = uiState.password,
                onPasswordChange = { model.setPassword(it) },
                onChangeVisibilityClick = { model.changePasswordVisibility() },
                passwordShown = uiState.passwordShown,
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrect = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { model.login() }
                )
            )
            Text(
                text = uiState.errorText,
                color = MaterialTheme.colorScheme.error
            )
            Button({ model.login() }) {
                Text("Login")
            }
            TextButton({ navigator push RegisterScreen(uiState.username, uiState.password) }) {
                Text(
                    text = "Don't have an account? Register here",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
@Preview
fun LoginScreenPreview()
{
    AppThemePreviewer {
        preview(previewTheme = PreviewTheme.Dark) {
            LoginScreen.Content()
        }
    }
}
