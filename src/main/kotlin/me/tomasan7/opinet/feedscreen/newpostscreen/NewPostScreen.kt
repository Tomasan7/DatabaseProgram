package me.tomasan7.opinet.feedscreen.newpostscreen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import me.tomasan7.opinet.feedscreen.Post
import me.tomasan7.opinet.feedscreen.toUser
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.ui.component.VerticalSpacer
import me.tomasan7.opinet.util.AppThemePreviewer

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
        val opiNet = navigator.getOpiNet()
        val model = rememberScreenModel { NewPostScreenModel(
            opiNet.postService,
            opiNet.currentUser!!.toUser(),
            editingPost
        ) }
        val uiState = model.uiState

        if (uiState.goBackToFeedEvent)
        {
            model.goBackToFeedEventConsumed()
            navigator.pop()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(300.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navigator.pop() }
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
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            VerticalSpacer(16.dp)
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
