package me.tomasan7.opinet.loginscreen

import StackedSnackbarAnimation
import StackedSnackbarDuration
import StackedSnackbarHost
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import me.tomasan7.opinet.managementscreen.ManagementScreen
import me.tomasan7.opinet.registerscreen.RegisterScreen
import me.tomasan7.opinet.ui.component.PasswordTextField
import me.tomasan7.opinet.ui.component.ScreenTitle
import me.tomasan7.opinet.ui.component.Tooltipped
import me.tomasan7.opinet.ui.component.VerticalSpacer
import me.tomasan7.opinet.util.AppThemePreviewer
import rememberStackedSnackbarHostState

object LoginScreen : Screen
{
    private fun readResolve(): Any = LoginScreen

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val opiNet = navigator.getOpiNet()
        val model = rememberScreenModel { LoginScreenModel(opiNet.userService, opiNet, sessionFile = opiNet.getConfig().sessionFile) }
        val uiState = model.uiState

        if (uiState.loginSuccessEvent)
        {
            model.loginSuccessEventConsumed()
            navigator push FeedScreen
        }

        val stackedSnackbarHostState = rememberStackedSnackbarHostState(
            maxStack = 1,
            animation = StackedSnackbarAnimation.Slide
        )

        LaunchedEffect(uiState.errorText) {
            if (uiState.errorText != null)
            {
                stackedSnackbarHostState.showErrorSnackbar(
                    title = uiState.errorText,
                    duration = StackedSnackbarDuration.Short
                )
                model.errorTextConsumed()
            }
        }

        Scaffold(
            snackbarHost = { StackedSnackbarHost(stackedSnackbarHostState) }
        ) { innerPadding ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScreenTitle("Login")
                    VerticalSpacer(16.dp)
                    TextField(
                        value = uiState.username,
                        singleLine = true,
                        onValueChange = { model.setUsername(it) },
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
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
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { model.login() }
                        )
                    )
                    RememberMe(
                        checked = uiState.rememberMe,
                        onClick = { model.setRememberMe(!uiState.rememberMe) }
                    )
                    VerticalSpacer(16.dp)
                    Button(
                        onClick = { model.login() },
                        enabled = uiState.username.isNotBlank() && uiState.password.isNotBlank()
                    ) {
                        Text("Login")
                    }
                    TextButton({ navigator push RegisterScreen(uiState.username, uiState.password) }) {
                        Text(
                            text = "Don't have an account? Register here",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Tooltipped(
                    tooltip = "Management",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ) {
                    IconButton(
                        onClick = { navigator push ManagementScreen }
                    ) {
                        Icon(Icons.Outlined.TableView, contentDescription = "management")
                    }
                }
            }
        }
    }

    @Composable
    private fun RememberMe(
        checked: Boolean,
        onClick: () -> Unit
    )
    {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onClick)
                .requiredHeight(ButtonDefaults.MinHeight)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )
            Text("Remember me")
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
