package me.tomasan7.opinet.managementscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.ui.component.VerticalSpacer
import kotlin.io.path.Path
import kotlin.io.path.absolute

object ManagementScreen : Screen
{
    private fun readResolve(): Any = ManagementScreen

    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val opiNet = navigator.getOpiNet()
        val model = rememberScreenModel { ManagementScreenModel(opiNet.getConfig().import, opiNet.userService, opiNet.postService) }
        val uiState = model.uiState

        val filePickerLauncher = rememberFilePickerLauncher(
            type = PickerType.File(listOf("csv")),
            initialDirectory = Path(".").absolute().toString(),
            mode = PickerMode.Multiple(maxItems = 10)
        ) { files ->
            if (files != null)
            {
                val paths = files.map { it.path!! }

                if (uiState.importUsers)
                {
                    model.onImportUsersFilesChosen(paths)
                }
                else if (uiState.importPosts)
                    model.onImportPostsFilesChosen(paths)
            }
            else
            {
                if (uiState.importUsers)
                    model.onImportUsersDismissed()
                else if (uiState.importPosts)
                    model.onImportPostsDismissed()
            }
        }

        LaunchedEffect(uiState.importUsers || uiState.importPosts) {
            if (uiState.importUsers || uiState.importPosts)
                filePickerLauncher.launch()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navigator.pop() }
                ) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Management",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            VerticalSpacer(50.dp)
            Button(
                onClick = { model.onImportUsersClicked() }
            ) {
                Text("Import users")
            }
            Button(
                onClick = { model.onImportPostsClicked() }
            ) {
                Text("Import posts")
            }
        }
    }
}
