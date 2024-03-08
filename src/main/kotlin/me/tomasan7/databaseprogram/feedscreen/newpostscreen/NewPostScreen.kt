package me.tomasan7.databaseprogram.feedscreen.newpostscreen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alexfacciorusso.previewer.PreviewTheme
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import me.tomasan7.databaseprogram.feedscreen.Post
import me.tomasan7.databaseprogram.getDatabaseProgram
import me.tomasan7.databaseprogram.util.AppThemePreviewer

data class NewPostScreen(
    /* Only set if we are editing an existing post */
    val editingPost: Post? = null,
    val oldTitle: String = "",
    val oldContent: String = ""
) : Screen
{
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val databaseProgram = navigator.getDatabaseProgram()
        val model = rememberScreenModel { NewPostScreenModel(databaseProgram.postService, databaseProgram.userService, databaseProgram, editingPost) }
        val uiState = model.uiState

        if (uiState.goBackToFeedEvent)
        {
            model.goBackToFeedEventConsumed()
            navigator.pop()
        }

        FilePicker(uiState.filePickerOpen, fileExtensions = listOf("csv")) { mpFile ->
            if (mpFile == null)
                model.closeImportFilePicker()
            else
                model.onImportFileChosen(mpFile.path)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(250.dp)
        ) {
            IconButton(
                onClick = { navigator.pop() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = "Back",
                )
            }
            Text(
                text = if (!uiState.isEditing) "Create new post" else "Edit post",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { model.setTitle(it) },
                singleLine = false,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.content,
                onValueChange = { model.setContent(it) },
                singleLine = false,
                label = { Text("Content") },
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            )
            Button({ model.submit() }) {
                Text(if (!uiState.isEditing) "Submit" else "Save")
            }
            TooltipBox(
                tooltip = {
                    PlainTooltip {
                        Text("Import posts from CSV file")
                    }
                },
                state = rememberTooltipState(),
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()
            ) {
                IconButton({ model.onImportClick() }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Import posts",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NewPostScreenPreview()
{
    AppThemePreviewer {
        preview(previewTheme = PreviewTheme.Dark) {
            NewPostScreen().Content()
        }
    }
}
