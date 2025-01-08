package me.tomasan7.opinet.report

import me.tomasan7.opinet.comment.CommentCountView
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.post.PostTable
import me.tomasan7.opinet.post.toPostDto
import me.tomasan7.opinet.service.DatabaseService
import me.tomasan7.opinet.user.UserDto
import me.tomasan7.opinet.user.UserTable
import me.tomasan7.opinet.user.toUser
import me.tomasan7.opinet.vote.PostAbsoluteVotesView
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class DatabaseReportService(
    database: Database
): ReportService, DatabaseService(database, UserActivityView, PostActivityView, PostAbsoluteVotesView, CommentCountView)
{
    override suspend fun getMostActiveUser(): IntReportDto<UserDto>?
    {
        return dbQuery {
            UserActivityView.join(
                otherTable = UserTable,
                joinType = JoinType.INNER,
                onColumn = UserTable.id,
                otherColumn = UserActivityView.userId
            ).selectAll()
                .orderBy(UserActivityView.activity, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.let {
                    IntReportDto(
                        entity = it.toUser(),
                        value = it[UserActivityView.activity]
                    )
                }
        }
    }

    override suspend fun getMostActivePost(): IntReportDto<PostDto>?
    {
        return dbQuery {
            PostActivityView.join(
                otherTable = PostTable,
                joinType = JoinType.INNER,
                onColumn = PostTable.id,
                otherColumn = PostActivityView.postId
            ).selectAll()
                .orderBy(PostActivityView.activity, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.let {
                    IntReportDto(
                        entity = it.toPostDto(),
                        value = it[PostActivityView.activity]
                    )
                }
        }
    }

    private suspend fun getFirstPostByVotes(order: SortOrder): IntReportDto<PostDto>?
    {
        return dbQuery {
            PostAbsoluteVotesView.join(
                otherTable = PostTable,
                joinType = JoinType.INNER,
                onColumn = PostTable.id,
                otherColumn = PostAbsoluteVotesView.postId
            ).selectAll()
                .orderBy(PostAbsoluteVotesView.votes, order)
                .limit(1)
                .singleOrNull()
                ?.let {
                    IntReportDto(
                        entity = it.toPostDto(),
                        value = it[PostAbsoluteVotesView.votes]
                    )
                }
        }
    }

    override suspend fun getMostUpvotedPost(): IntReportDto<PostDto>?
    {
        return getFirstPostByVotes(SortOrder.DESC)
    }

    override suspend fun getMostDownvotedPost(): IntReportDto<PostDto>?
    {
        return getFirstPostByVotes(SortOrder.ASC)
    }

    override suspend fun getMostCommentedPost(): IntReportDto<PostDto>?
    {
        return dbQuery {
            CommentCountView.join(
                otherTable = PostTable,
                joinType = JoinType.INNER,
                onColumn = PostTable.id,
                otherColumn = CommentCountView.postId
            ).selectAll()
                .orderBy(CommentCountView.commentCount, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.let {
                    IntReportDto(
                        entity = it.toPostDto(),
                        value = it[CommentCountView.commentCount]
                    )
                }
        }
    }
}
