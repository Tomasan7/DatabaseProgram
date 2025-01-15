package me.tomasan7.opinet.feedscreen

import StackedSnackbarAnimation
import StackedSnackbarDuration
import StackedSnackbarHost
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
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
import me.tomasan7.opinet.feedscreen.commentdialog.CommentsDialog
import me.tomasan7.opinet.feedscreen.newpostscreen.NewPostScreen
import me.tomasan7.opinet.friendscreen.FriendScreen
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.loginscreen.LoginScreen
import me.tomasan7.opinet.ui.component.ScreenTitle
import me.tomasan7.opinet.ui.component.Tooltipped
import me.tomasan7.opinet.ui.component.VerticalSpacer
import me.tomasan7.opinet.util.AppThemePreviewer
import rememberStackedSnackbarHostState

object FeedScreen : Screen
{
    private fun readResolve(): Any = FeedScreen

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
                opiNet.voteService,
                opiNet.currentUser!!.toUser()
            )
        }
        val uiState = model.uiState
        val currentUser = remember { opiNet.currentUser!!.toUser() }

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

        val stackedSnackbarHostState = rememberStackedSnackbarHostState(
            maxStack = 1,
            animation = StackedSnackbarAnimation.Slide
        )

        LaunchedEffect(uiState.errorText) {
            if (uiState.errorText != null) {
                stackedSnackbarHostState.showErrorSnackbar(
                    title = uiState.errorText,
                    duration = StackedSnackbarDuration.Short
                )
                model.onEventErrorConsumed()
            }
        }

        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { navigator push NewPostScreen() },
                    text = { Text("New post") },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "New post") }
                )
            },
            snackbarHost = { StackedSnackbarHost(stackedSnackbarHostState) }
        ) { contentPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = 32.dp)
                    .fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScreenTitle(
                            title = "Posts feed",
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.Center)
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {

                            SecondaryTabRow(
                                selectedTabIndex = uiState.tab.ordinal,
                                modifier = Modifier.width(200.dp)
                            ) {
                                Tab(
                                    selected = uiState.tab == FeedScreenTab.ALL,
                                    onClick = { model.setTab(FeedScreenTab.ALL) },
                                    text = { Text("All") }
                                )
                                Tab(
                                    selected = uiState.tab == FeedScreenTab.FRIENDS,
                                    onClick = { model.setTab(FeedScreenTab.FRIENDS) },
                                    text = { Text("Friends") }
                                )
                            }

                            Row {
                                Tooltipped("Friends") {
                                    IconButton({ navigator push FriendScreen }) {
                                        Icon(Icons.Default.Group, "Friends")
                                    }
                                }

                                LoggedUser(
                                    user = currentUser,
                                    onLogout = {
                                        opiNet.logout()
                                        navigator.popUntil { it is LoginScreen }
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.loading)
                        CircularProgressIndicator()
                    if (!uiState.loading && uiState.posts.isEmpty())
                        Text("No posts to show")
                    else
                        uiState.posts.forEach { post ->
                            key(post.id) {
                                Post(
                                    post = post,
                                    owned = opiNet.currentUser!!.id == post.author.id,
                                    onEditClick = { model.editPost(post) },
                                    onDeleteClick = { model.deletePost(post) },
                                    onVote = { model.voteOnPost(post, it) },
                                    onCommentClick = { model.openComments(post.id) }
                                )
                            }
                        }
                    VerticalSpacer(100.dp)
                }
            }
        }
    }
}

@Composable
private fun User(user: User, onClick: () -> Unit = {})
{
    Surface(
        shape = RoundedCornerShape(100),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = Modifier,
        onClick = onClick
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
private fun LoggedUser(user: User, onLogout: () -> Unit)
{
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        User(
            user = user,
            onClick = { menuExpanded = !menuExpanded }
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    menuExpanded = false
                    onLogout()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Logout,
                        contentDescription = "Logout"
                    )
                }
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
