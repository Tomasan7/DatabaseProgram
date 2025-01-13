package me.tomasan7.opinet.feedscreen

import kotlinx.datetime.LocalDate
import me.tomasan7.opinet.post.PostDto

data class Post(
    val title: String,
    val content: String,
    val author: User,
    val uploadDate: LocalDate,
    val commentCount: Int,
    val voted: Boolean?,
    val upVotes: Int,
    val downVotes: Int,
    val public: Boolean,
    val id: Int
)

inline fun PostDto.toPost(
    voted: Boolean?,
    authorGetter: (Int) -> User,
    commentCountGetter: (Int) -> Int,
    votesGetter: (Int) -> Pair<Int, Int>
) = votesGetter(id!!).let { (upVotes, downVotes) ->
    Post(
        title = title,
        content = content,
        author = authorGetter(authorId),
        commentCount = commentCountGetter(id),
        uploadDate = uploadDate,
        voted = voted,
        upVotes = upVotes,
        downVotes = downVotes,
        public = public,
        id = id
    )
}
