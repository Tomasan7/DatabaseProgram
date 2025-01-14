package me.tomasan7.opinet.feedscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.comment.CommentDto
import me.tomasan7.opinet.comment.CommentService
import me.tomasan7.opinet.post.PostService
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.util.isNetworkError
import me.tomasan7.opinet.util.replace
import me.tomasan7.opinet.vote.VoteDto
import me.tomasan7.opinet.vote.VoteService

class FeedScreenModel(
    private val userService: UserService,
    private val postService: PostService,
    private val commentService: CommentService,
    private val voteService: VoteService,
    private val currentUser: User
) : ScreenModel
{
    var uiState by mutableStateOf(FeedScreenState())
        private set

    private val cachedUsers = mutableMapOf<Int, User>(currentUser.id to currentUser)
    private var loadJob: Job? = null

    fun loadPosts()
    {
        loadJob?.cancel()
        loadJob = screenModelScope.launch {
            try
            {
                val postDtos =
                    if (uiState.tab == FeedScreenTab.ALL) postService.getAllPostsVisibleToOrderedByUploadDateDesc(
                        currentUser.id
                    )
                    else postService.getPrivatePostsVisibleToOrderedByUploadDateDesc(currentUser.id)
                val posts = postDtos.map { postDto ->
                    val votesOnPost = voteService.getVotesOnPost(postDto.id!!)
                    val voted = votesOnPost.find { it.userId == currentUser.id }?.upDown
                    postDto.toPost(
                        authorGetter = { userId -> getUser(userId) },
                        voted = voted,
                        commentCountGetter = { commentService.getNumberOfCommentsForPost(postDto.id).toInt() },
                        votesGetter = { votesOnPost.count { it.upDown } to votesOnPost.count { !it.upDown } }
                    )
                }.toImmutableList()
                changeUiState(posts = posts)
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

    fun editPost(post: Post)
    {
        changeUiState(editPostEvent = post)
    }

    fun onEventErrorConsumed()
    {
        changeUiState(errorText = null)
    }

    fun editPostEventConsumed()
    {
        changeUiState(editPostEvent = null)
    }

    fun setTab(tab: FeedScreenTab)
    {
        changeUiState(tab = tab, posts = persistentListOf())
        loadPosts()
    }

    fun deletePost(post: Post)
    {
        screenModelScope.launch {
            try
            {
                val result = postService.deletePost(post.id)
                if (result)
                    changeUiState(posts = (uiState.posts - post).toImmutableList())
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

    fun openComments(postId: Int)
    {
        screenModelScope.launch {
            try
            {
                val commentsForPost = commentService
                    .getAllCommentsForPostOrderedByUploadDateDesc(postId)
                    .map { it.toComment { userId -> getUser(userId) } }
                    .toImmutableList()
                val newCommentsDialogState = FeedScreenState.CommentsDialogState(
                    postId = postId,
                    comments = commentsForPost,
                    isOpen = true
                )
                changeUiState(commentsDialogState = newCommentsDialogState)
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

    fun closeComments()
    {
        changeUiState(commentsDialogState = uiState.commentsDialogState.copy(isOpen = false))
    }

    fun postComment(commentText: String, postId: Int)
    {
        screenModelScope.launch {
            try
            {
                val commentDto = CommentDto(
                    text = commentText,
                    authorId = currentUser.id,
                    uploadDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    postId = postId
                )
                val newCommentId = commentService.createComment(commentDto)
                val newCommentDto = commentDto.copy(id = newCommentId).toComment { getUser(it) }
                val oldComments = uiState.commentsDialogState.comments
                val newComments = (oldComments + newCommentDto).toImmutableList()
                val oldPost = uiState.posts.find { it.id == postId }!!
                val newPost = oldPost.copy(
                    commentCount = oldPost.commentCount + 1
                )
                val newPosts = uiState.posts.replace(oldPost, newPost)
                changeUiState(
                    commentsDialogState = uiState.commentsDialogState.copy(comments = newComments),
                    posts = newPosts.toImmutableList()
                )
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

    fun voteOnPost(post: Post, value: Boolean?)
    {
        if (value == null)
            removeVoteOnPost(post)
        else
            addVoteOnPost(post, value)
    }

    private fun addVoteOnPost(post: Post, value: Boolean)
    {
        screenModelScope.launch {
            try
            {
                val previousVote = post.voted
                if (previousVote != null)
                    voteService.removeVoteByUserOnPost(currentUser.id, post.id)
                val voteDto = VoteDto(
                    upDown = value,
                    votedAt = Clock.System.now(),
                    userId = currentUser.id,
                    postId = post.id
                )
                voteService.createVote(voteDto)
                val newPost = post.copy(
                    voted = value,
                    upVotes = post.upVotes + if (value) 1 else 0 - if (previousVote == true) 1 else 0,
                    downVotes = post.downVotes + if (!value) 1 else 0 - if (previousVote == false) 1 else 0
                )
                changeUiState(posts = uiState.posts.replace(post, newPost).toImmutableList())
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

    private fun removeVoteOnPost(post: Post)
    {
        screenModelScope.launch {
            try
            {
                val result = voteService.removeVoteByUserOnPost(currentUser.id, post.id)
                if (result)
                {
                    val newPost = post.copy(
                        voted = null,
                        upVotes = post.upVotes - if (post.voted!!) 1 else 0,
                        downVotes = post.downVotes - if (!post.voted) 1 else 0
                    )
                    changeUiState(posts = uiState.posts.replace(post, newPost).toImmutableList())
                }
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

    private suspend fun getUser(id: Int): User
    {
        return cachedUsers.getOrPut(id) {
            userService.getUserById(id)!!.toUser()
        }
    }

    private fun changeUiState(
        posts: ImmutableList<Post> = uiState.posts,
        tab: FeedScreenTab = uiState.tab,
        commentsDialogState: FeedScreenState.CommentsDialogState = uiState.commentsDialogState,
        errorText: String? = uiState.errorText,
        editPostEvent: Post? = uiState.editPostEvent
    )
    {
        uiState = uiState.copy(
            posts = posts,
            tab = tab,
            commentsDialogState = commentsDialogState,
            errorText = errorText,
            editPostEvent = editPostEvent
        )
    }
}
