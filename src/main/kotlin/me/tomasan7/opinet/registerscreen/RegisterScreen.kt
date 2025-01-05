package me.tomasan7.opinet.registerscreen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alexfacciorusso.previewer.PreviewTheme
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import me.tomasan7.opinet.feedscreen.FeedScreen
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.loginscreen.LoginScreen
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
                opiNet,
                opiNet.getConfig().import
            )
        }
        val uiState = model.uiState

        if (uiState.registrationSuccessEvent)
        {
            model.registrationSuccessEventConsumed()
            navigator push FeedScreen
        }

        FilePicker(uiState.filePickerOpen, fileExtensions = listOf("csv")) { mpFile ->
            if (mpFile == null)
                model.closeImportFilePicker()
            else
                model.onImportFileChosen(mpFile.path)
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

            var genderMenuExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = genderMenuExpanded,
                onExpandedChange = { genderMenuExpanded = it }
            ) {
                TextField(
                    value = uiState.gender.toString(),
                    singleLine = true,
                    readOnly = true,
                    onValueChange = {},
                    label = { Text("gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = genderMenuExpanded,
                    onDismissRequest = { genderMenuExpanded = false }
                ) {
                    Gender.entries.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender.toString()) },
                            onClick = {
                                model.setGender(gender)
                                genderMenuExpanded = false
                            }
                        )
                    }
                }
            }

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
            TooltipBox(
                tooltip = {
                    PlainTooltip {
                        Text("Import users from CSV file")
                    }
                },
                state = rememberTooltipState(),
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()
            ) {
                IconButton({ model.onImportClick() }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Import users",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
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
