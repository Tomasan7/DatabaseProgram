package me.tomasan7.databaseprogram.feedscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.tomasan7.databaseprogram.DatabaseProgram
import me.tomasan7.databaseprogram.post.PostService
import me.tomasan7.databaseprogram.user.UserDto
import me.tomasan7.databaseprogram.user.UserService
import me.tomasan7.databaseprogram.user.UserTable.firstName
import me.tomasan7.databaseprogram.user.UserTable.lastName
import me.tomasan7.databaseprogram.user.UserTable.password
import me.tomasan7.databaseprogram.user.UserTable.username

class FeedScreenModel(
    private val userService: UserService,
    private val postService: PostService,
    private val databaseProgram: DatabaseProgram
) : ScreenModel
{
    var uiState by mutableStateOf(FeedScreenState())
        private set

    private val cachedUsers = mutableMapOf<Int, UserDto>(
        databaseProgram.currentUser.id!! to databaseProgram.currentUser
    )

    fun loadPosts()
    {
        screenModelScope.launch {
            val posts = postService.getAllPosts().map { postDto ->
                postDto.toPost { userId -> getUserDto(userId).toUser() }
            }.toImmutableList()
            changeUiState(posts = posts)
        }
    }

    private suspend fun getUserDto(id: Int): UserDto
    {
        return cachedUsers.getOrPut(id) {
            userService.getUserById(id)!!
        }
    }

    private fun changeUiState(
        posts: ImmutableList<Post>? = null
    )
    {
        uiState = uiState.copy(
            posts = posts ?: uiState.posts
        )
    }
}