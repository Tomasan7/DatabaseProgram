package me.tomasan7.databaseprogram.loginpage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alexfacciorusso.previewer.PreviewTheme
import me.tomasan7.databaseprogram.registerscreen.RegisterScreen
import me.tomasan7.databaseprogram.ui.AppThemePreviewer
import me.tomasan7.databaseprogram.ui.component.PasswordTextField
import me.tomasan7.databaseprogram.ui.component.VerticalSpacer

object LoginScreen : Screen
{
    private fun readResolve(): Any = LoginScreen

    @Composable
    override fun Content()
    {
        val model = rememberScreenModel { LoginScreenModel() }
        val uiState = model.uiState
        val navigator = LocalNavigator.currentOrThrow

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
                label = { Text("Username") }
            )
            PasswordTextField(
                password = uiState.password,
                onPasswordChange = { model.setPassword(it) },
                onChangeVisibilityClick = { model.changePasswordVisibility() },
                passwordShown = uiState.passwordShown,
                label = { Text("Password") }
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