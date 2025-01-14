package me.tomasan7.opinet.friendscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.sksamuel.hoplite.transformer.PathNormalizer.transform
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.friend.FriendService
import me.tomasan7.opinet.user.UserDto
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.util.isNetworkError
import me.tomasan7.opinet.util.replace

class FriendScreenModel(
    private val friendService: FriendService,
    private val userService: UserService,
    private val currentUser: UserDto
) : ScreenModel
{
    var uiState by mutableStateOf(FriendScreenState())
        private set

    init
    {
        loadFriends()
    }

    fun setUserSearch(value: String) = changeUiState(userSearch = value)

    fun sendRequest(user: UserDto)
    {
        screenModelScope.launch {
            try
            {
                friendService.requestFriendship(currentUser.id!!, user.id!!)
                loadFriends()
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                    changeUiState(errorText = Messages.networkError)
                else if (e is CancellationException)
                    throw e
                else
                    e.printStackTrace()
            }
        }
    }

    private fun List<MaybeFriend>.update(userId: Int, update: (MaybeFriend) -> MaybeFriend) = this.map {
        if (it.user.id == userId)
            update(it)
        else
            it
    }

    fun removeFriend(friend: UserDto)
    {
        screenModelScope.launch {
            try
            {
                friendService.removeFriendship(currentUser.id!!, friend.id!!)
                changeUiState(users = uiState.users.update(friend.id) { it.copy(isFriend = false) })
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                    changeUiState(errorText = Messages.networkError)
                else if (e is CancellationException)
                    throw e
                else
                    e.printStackTrace()
            }
        }
    }

    fun acceptRequest(user: UserDto)
    {
        screenModelScope.launch {
            try
            {
                friendService.acceptRequest(user.id!!, currentUser.id!!)
                changeUiState(users = uiState.users.update(user.id) { it.copy(incomingRequest = false, isFriend = true) })
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                    changeUiState(errorText = Messages.networkError)
                else if (e is CancellationException)
                    throw e
                else
                    e.printStackTrace()
            }
        }
    }

    fun rejectRequest(user: UserDto)
    {
        screenModelScope.launch {
            try
            {
                friendService.rejectRequest(user.id!!, currentUser.id!!)
                changeUiState(users = uiState.users.update(user.id) { it.copy(incomingRequest = false) })
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                    changeUiState(errorText = Messages.networkError)
                else if (e is CancellationException)
                    throw e
                else
                    e.printStackTrace()
            }
        }
    }

    private fun loadFriends()
    {
        screenModelScope.launch {
            try
            {
                val friendsMaybe = withContext(SupervisorJob(coroutineContext[Job])) {
                    val users = async { userService.getAllUsers().filter { it.id != currentUser.id!! } }
                    val friends = async { friendService.getFriendsOf(currentUser.id!!) }
                    val incomingFriendRequests = async { friendService.getIncomingRequestsFor(currentUser.id!!) }
                    val outgoingFriendRequests = async { friendService.getOutgoingRequestsFrom(currentUser.id!!) }

                    users.await().map { userDto ->
                        val isFriend = friends.await().any { it.id == userDto.id }
                        val isIncomingRequest = incomingFriendRequests.await().any { it.id == userDto.id }
                        val isOutgoingRequest = outgoingFriendRequests.await().any { it.id == userDto.id }

                        MaybeFriend(userDto, isFriend, isIncomingRequest, isOutgoingRequest)
                    }
                }
                changeUiState(users = friendsMaybe)
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                    changeUiState(errorText = Messages.networkError)
                else if (e is CancellationException)
                    throw e
                else
                    e.printStackTrace()
            }
        }
    }

    private fun changeUiState(
        users: List<MaybeFriend> = uiState.users,
        userSearch: String = uiState.userSearch,
        errorText: String? = uiState.errorText
    )
    {
        uiState = FriendScreenState(
            users = users,
            userSearch = userSearch,
            errorText = errorText
        )
    }
}
