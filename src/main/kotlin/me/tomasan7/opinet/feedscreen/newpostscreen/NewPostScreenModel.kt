package me.tomasan7.opinet.feedscreen.newpostscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tomasan7.opinet.config.Config
import me.tomasan7.opinet.feedscreen.Post
import me.tomasan7.opinet.feedscreen.User
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.post.PostService
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.util.isNetworkError

class NewPostScreenModel(
    private val postService: PostService,
    private val currentUser: User,
    private val editingPost: Post?,
) : ScreenModel
{
    var uiState by mutableStateOf(NewPostScreenState(
        isEditing = editingPost != null,
        title = editingPost?.title ?: "",
        content = editingPost?.content ?: ""
    ))
        private set

    fun setTitle(title: String) = changeUiState(title = title)

    fun setContent(content: String) = changeUiState(content = content)

    fun goBackToFeedEventConsumed() = changeUiState(goBackToFeedEvent = false)

    fun errorEventConsumed() = changeUiState(errorText = null)

    fun submit()
    {
        if (editingPost != null)
            submitPostUpdate()
        else
            submitPost()
    }

    private fun submitPost()
    {
        if (uiState.title.isBlank())
        {
            changeUiState(errorText = "Title cannot be blank")
            return
        }
        else if (uiState.content.isBlank())
        {
            changeUiState(errorText = "Content cannot be blank")
            return
        }

        val postDto = PostDto(
            title = uiState.title,
            content = uiState.content,
            uploadDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            authorId = currentUser.id
        )

        screenModelScope.launch {
            try
            {
                postService.createPost(postDto)
                changeUiState(goBackToFeedEvent = true)
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                    changeUiState(errorText = "There was an error connecting to the database, check your internet connection")
                else if (e is CancellationException)
                    throw e
                else
                    e.printStackTrace()
            }
        }
    }

    private fun submitPostUpdate()
    {
        val postDto = PostDto(
            id = editingPost!!.id,
            title = uiState.title,
            content = uiState.content,
            uploadDate = editingPost.uploadDate,
            authorId = editingPost.author.id
        )

        screenModelScope.launch {
            try
            {
                postService.updatePost(postDto)
                changeUiState(goBackToFeedEvent = true)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    private fun changeUiState(
        title: String = uiState.title,
        content: String = uiState.content,
        goBackToFeedEvent: Boolean = uiState.goBackToFeedEvent,
        errorText: String? = uiState.errorText
    )
    {
        uiState = uiState.copy(
            title = title,
            content = content,
            goBackToFeedEvent = goBackToFeedEvent,
            errorText = errorText
        )
    }
}
