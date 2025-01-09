package me.tomasan7.opinet.feedscreen

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class FeedScreenState(
    val posts: ImmutableList<Post> = persistentListOf(),
    val commentsDialogState: CommentsDialogState = CommentsDialogState(),
    val errorText: String? = null,
    val editPostEvent: Post? = null
)
{
    data class CommentsDialogState(
        val comments: ImmutableList<Comment> = persistentListOf(),
        val postId: Int? = null,
        val isOpen: Boolean = false
    )
}
