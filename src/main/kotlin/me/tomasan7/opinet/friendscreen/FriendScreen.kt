package me.tomasan7.opinet.friendscreen

import StackedSnackbarAnimation
import StackedSnackbarDuration
import StackedSnackbarHost
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.ui.component.ScreenTitle
import me.tomasan7.opinet.ui.component.Tooltipped
import me.tomasan7.opinet.user.UserDto
import rememberStackedSnackbarHostState

object FriendScreen : Screen
{
    private fun readResolve(): Any = FriendScreen

    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val opiNet = navigator.getOpiNet()
        val model =
            rememberScreenModel { FriendScreenModel(opiNet.friendService, opiNet.userService, opiNet.currentUser!!) }
        val uiState = model.uiState


        val stackedSnackbarHostState = rememberStackedSnackbarHostState(
            maxStack = 1,
            animation = StackedSnackbarAnimation.Slide
        )

        LaunchedEffect(uiState.errorText) {
            if (uiState.errorText != null)
                stackedSnackbarHostState.showErrorSnackbar(uiState.errorText, duration = StackedSnackbarDuration.Short)
        }

        Scaffold(
            snackbarHost = { StackedSnackbarHost(stackedSnackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                ScreenTitle(
                    title = "Friends",
                    onBackClick = { navigator.pop() }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    val notFriends by derivedStateOf { uiState.users.filter{!it.isFriend} }
                    val friends by derivedStateOf { uiState.users.filter { it.isFriend } }

                    PeopleSearch(
                        searchValue = uiState.userSearch,
                        onSearchValueChange = { model.setUserSearch(it) },
                        onUserSendRequest = { model.sendRequest(it) },
                        onUserAcceptRequest = { model.acceptRequest(it) },
                        notFriends = notFriends
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Friends(
                            friends = friends,
                            onRemoveFriend = { model.removeFriend(it) }
                        )

                        val incomingRequests by derivedStateOf { notFriends.filter { it.incomingRequest } }

                        IncomingRequests(
                            incomingRequests = incomingRequests,
                            onAccept = { model.acceptRequest(it) },
                            onReject = { model.rejectRequest(it) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun IncomingRequests(
        incomingRequests: List<MaybeFriend>,
        onAccept: (UserDto) -> Unit,
        onReject: (UserDto) -> Unit,
        modifier: Modifier = Modifier
    )
    {
        Column(
            modifier = modifier
        ) {
            Text(
                text = "Pending requests (${incomingRequests.size})",
                style = MaterialTheme.typography.headlineMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                incomingRequests.forEach {
                    PendingRequest(
                        maybeFriend = it,
                        onAccept = { onAccept(it.user) },
                        onReject = { onReject(it.user) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    @Composable
    private fun Friends(
        friends: List<MaybeFriend>,
        onRemoveFriend: (UserDto) -> Unit,
        modifier: Modifier = Modifier
    )
    {
        Column(
            modifier = modifier
        ) {
            Text(
                text = "Friends (${friends.size})",
                style = MaterialTheme.typography.headlineMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                friends.forEach {
                    Friend(
                        friend = it.user,
                        modifier = Modifier.fillMaxWidth(),
                        onRemoveFriend = { onRemoveFriend(it.user) }
                    )
                }
            }
        }
    }

    @Composable
    private fun PendingRequest(
        maybeFriend: MaybeFriend,
        onAccept: () -> Unit,
        onReject: () -> Unit,
        modifier: Modifier = Modifier
    )
    {
        Surface(
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp).fillMaxWidth()
            ) {
                Text(maybeFriend.user.firstName + " " + maybeFriend.user.lastName)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    IconButton(
                        onClick = onReject
                    ) {
                        Icon(Icons.Default.Close, "Reject", tint = Color(0xffd36060))
                    }

                    IconButton(
                        onClick = onAccept
                    ) {
                        Icon(Icons.Default.Check, "Accept", tint = Color(0xff7cc46e))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Friend(
        friend: UserDto,
        modifier: Modifier,
        onRemoveFriend: () -> Unit
    )
    {
        Surface(
            tonalElevation = 4.dp,
            modifier = modifier
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
            ) {
                Text(text = friend.firstName + " " + friend.lastName)

                Tooltipped(
                    tooltip = "Remove friend"
                ) {
                    IconButton(
                        onClick = onRemoveFriend
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove friend",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PeopleSearch(
        searchValue: String,
        onSearchValueChange: (String) -> Unit,
        onUserSendRequest: (UserDto) -> Unit,
        onUserAcceptRequest: (UserDto) -> Unit,
        notFriends: List<MaybeFriend>,
        modifier: Modifier = Modifier
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            OutlinedTextField(
                value = searchValue,
                placeholder = { Text("Find people") },
                onValueChange = onSearchValueChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            )

            val searchNotFriends by derivedStateOf {
                if (searchValue.isBlank())
                    emptyList()
                else
                    notFriends.filter {
                        it.user.firstName.lowercase().startsWith(searchValue.lowercase()) ||
                                it.user.lastName.lowercase().startsWith(searchValue.lowercase()) ||
                                it.user.username.lowercase().startsWith(searchValue.lowercase())
                    }
            }

                Column(
                    modifier = Modifier
                        .height(300.dp)
                        .width(IntrinsicSize.Max)
                        .verticalScroll(rememberScrollState())
                ) {
                    searchNotFriends.forEach {
                        SearchUser(
                            maybeFriend = it,
                            onSendRequest = onUserSendRequest,
                            onAcceptRequest = onUserAcceptRequest,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
        }
    }

    @Composable
    private fun SearchUser(
        maybeFriend: MaybeFriend,
        onSendRequest: (UserDto) -> Unit,
        onAcceptRequest: (UserDto) -> Unit,
        modifier: Modifier
    )
    {
        Surface(
            tonalElevation = 4.dp,
            modifier = modifier
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
            ) {
                Text(text = maybeFriend.user.firstName + " " + maybeFriend.user.lastName)

                TextButton(
                    onClick = {
                        if (maybeFriend.incomingRequest)
                            onAcceptRequest(maybeFriend.user)
                        else
                            onSendRequest(maybeFriend.user)
                    },
                    enabled = !maybeFriend.outgoingRequest
                ) {
                    if (maybeFriend.incomingRequest)
                        Text("Accept request")
                    else if (maybeFriend.outgoingRequest)
                        Text("Request sent")
                    else
                        Text("Send request")
                }
            }
        }
    }
}
