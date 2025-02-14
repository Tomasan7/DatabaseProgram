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
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.config.Config
import me.tomasan7.opinet.feedscreen.Post
import me.tomasan7.opinet.feedscreen.User
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.post.PostService
import me.tomasan7.opinet.post.PostTable
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.util.isNetworkError
import me.tomasan7.opinet.util.size
import me.tomasan7.opinet.util.trimAndCut

class NewPostScreenModel(
    private val postService: PostService,
    private val currentUser: User,
    private val editingPost: Post?,
) : ScreenModel
{
    var uiState by mutableStateOf(NewPostScreenState(
        // TODO: ScreenModel should not directly depend on Model implementation. Move this logic to abstract service.
        maxLengths = NewPostScreenState.MaxLengths(
            title = PostTable.title.size,
            content = 1000
        ),
        isEditing = editingPost != null,
        title = editingPost?.title ?: "",
        content = editingPost?.content ?: "",
        public = editingPost?.public == true
    ))
        private set

    fun setTitle(title: String) = changeUiState(title = title.trimAndCut(uiState.maxLengths.title))

    fun setContent(content: String) = changeUiState(content = content.trimAndCut(uiState.maxLengths.content))

    fun goBackToFeedEventConsumed() = changeUiState(goBackToFeedEvent = false)

    fun setPublic(value: Boolean) = changeUiState(public = value)

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
            public = uiState.public,
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
                    changeUiState(errorText = Messages.networkError)
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
            public = uiState.public,
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
        title: String = uiState.title,
        content: String = uiState.content,
        public: Boolean = uiState.public,
        goBackToFeedEvent: Boolean = uiState.goBackToFeedEvent,
        errorText: String? = uiState.errorText
    )
    {
        uiState = uiState.copy(
            title = title,
            public = public,
            content = content,
            goBackToFeedEvent = goBackToFeedEvent,
            errorText = errorText
        )
    }
}
