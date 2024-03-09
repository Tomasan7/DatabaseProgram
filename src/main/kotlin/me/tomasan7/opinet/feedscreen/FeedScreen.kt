package me.tomasan7.opinet.feedscreen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.alexfacciorusso.previewer.PreviewTheme
import me.tomasan7.opinet.feedscreen.commentdialog.CommentsDialog
import me.tomasan7.opinet.feedscreen.newpostscreen.NewPostScreen
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.ui.component.VerticalSpacer
import me.tomasan7.opinet.util.AppThemePreviewer

object FeedScreen : Screen
{
    private fun readResolve(): Any = FeedScreen

    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val opiNet = navigator.getOpiNet()
        val model = rememberScreenModel {
            FeedScreenModel(
                opiNet.userService,
                opiNet.postService,
                opiNet.commentService,
                opiNet.votesService,
                opiNet.currentUser.toUser()
            )
        }
        val uiState = model.uiState
        val currentUser = remember { opiNet.currentUser.toUser() }

        LaunchedEffect(Unit) {
            model.loadPosts()
        }

        if (uiState.commentsDialogState.isOpen
            && uiState.commentsDialogState.postId != null
        )
            CommentsDialog(
                comments = uiState.commentsDialogState.comments,
                onPostComment = { commentText ->
                    model.postComment(commentText, uiState.commentsDialogState.postId)
                },
                onDismissRequest = {
                    model.closeComments()
                }
            )

        if (uiState.editPostEvent != null)
        {
            model.editPostEventConsumed()
            navigator push NewPostScreen(editingPost = uiState.editPostEvent)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Posts feed",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
                uiState.posts.forEach { post ->
                    key(post.id) {
                        Post(
                            post = post,
                            owned = opiNet.currentUser.id == post.author.id,
                            onEditClick = { model.editPost(post) },
                            onDeleteClick = { model.deletePost(post) },
                            onVote = { model.voteOnPost(post, it) },
                            onCommentClick = { model.openComments(post.id) }
                        )
                    }
                }
            }
            VerticalSpacer(16.dp)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                User(user = currentUser)
                FloatingActionButton({ navigator push NewPostScreen() }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    }
}

@Composable
private fun User(user: User)
{
    Surface(
        shape = RoundedCornerShape(100),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${user.firstName} ${user.lastName}"
            )
        }
    }
}

@Composable
@Preview
fun LoginScreenPreview()
{
    AppThemePreviewer {
        preview(previewTheme = PreviewTheme.Dark) {
            FeedScreen.Content()
        }
    }
}
