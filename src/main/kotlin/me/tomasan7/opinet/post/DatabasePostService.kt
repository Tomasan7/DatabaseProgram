package me.tomasan7.opinet.post

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import me.tomasan7.opinet.comment.CommentService
import me.tomasan7.opinet.friend.FriendTable
import me.tomasan7.opinet.service.DatabaseService
import me.tomasan7.opinet.user.Gender
import me.tomasan7.opinet.user.UserTable
import me.tomasan7.opinet.vote.VoteService
import me.tomasan7.opinet.vote.VoteTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import sun.tools.jconsole.Messages.IS
import javax.swing.DropMode.ON

class DatabasePostService(
    database: Database,
    private val commentService: CommentService,
    private val voteService: VoteService
) : PostService, DatabaseService(database, PostTable)
{
    override suspend fun createPost(postDto: PostDto): Int
    {
        if (postDto.id != null)
            throw IllegalArgumentException("Post id must be null when creating a new post")

        return dbQuery {
            val postId = PostTable.insertAndGetId {
                it[title] = postDto.title
                it[public] = postDto.public
                it[content] = postDto.content
                it[uploadDate] = postDto.uploadDate
                it[authorId] = postDto.authorId
            }.value
            VoteTable.insert {
                it[VoteTable.postId] = postId
                it[VoteTable.userId] = postDto.authorId
                it[VoteTable.upDown] = true
                it[VoteTable.votedAt] = Clock.System.now().epochSeconds.toUInt()
            }
            postId
        }
    }

    override suspend fun getPostById(id: Int): PostDto? = dbQuery {
        PostTable.selectAll()
            .where { PostTable.id eq id }
            .singleOrNull()
            ?.toPostDto()
    }

    override suspend fun getAllPostsOrderedByUploadDateDesc() = dbQuery {
        PostTable.selectAll()
            .orderBy(PostTable.uploadDate to SortOrder.DESC)
            .map { it.toPostDto() }
            .toImmutableList()
    }

    override suspend fun getAllPostsVisibleToOrderedByUploadDateDesc(userId: Int): ImmutableList<PostDto>
    {
        val f1 = FriendTable.alias("f1")
        val f2 = FriendTable.alias("f2")

        return dbQuery {
            PostTable.leftJoin(
                otherTable = f1,
                additionalConstraint = { PostTable.authorId eq f1[FriendTable.requesterId] }
            ).leftJoin(
                otherTable = f2,
                additionalConstraint = { (PostTable.authorId eq f2[FriendTable.targetId]) and (f1[FriendTable.targetId] eq f2[FriendTable.requesterId]) }
            ).selectAll()
                .withDistinct()
                .where {
                    (PostTable.public eq true) or
                            (PostTable.authorId eq userId) or
                            ((f1[FriendTable.requesterId] eq userId) and (f2[FriendTable.targetId] eq userId)) or
                            ((f1[FriendTable.targetId] eq userId) and (f2[FriendTable.requesterId] eq userId))
                }
                .orderBy(PostTable.uploadDate to SortOrder.DESC)
                .map { it.toPostDto() }
                .toImmutableList()
        }
    }

    override suspend fun getPrivatePostsVisibleToOrderedByUploadDateDesc(userId: Int): ImmutableList<PostDto>
    {
        val f1 = FriendTable.alias("f1")
        val f2 = FriendTable.alias("f2")

        // TODO: DRY: these two (getXPosts..) selects are the same except public check
        return dbQuery {
            PostTable.leftJoin(
                otherTable = f1,
                additionalConstraint = { PostTable.authorId eq f1[FriendTable.requesterId] }
            ).leftJoin(
                otherTable = f2,
                additionalConstraint = { (PostTable.authorId eq f2[FriendTable.targetId]) and (f1[FriendTable.targetId] eq f2[FriendTable.requesterId]) }
            ).selectAll()
                .withDistinct()
                .where {
                    (PostTable.authorId eq userId) or
                    ((f1[FriendTable.requesterId] eq userId) and (f2[FriendTable.targetId] eq userId)) or
                            ((f1[FriendTable.targetId] eq userId) and (f2[FriendTable.requesterId] eq userId))
                }
                .orderBy(PostTable.uploadDate to SortOrder.DESC)
                .map { it.toPostDto() }
                .toImmutableList()
        }
    }

    override suspend fun getPostsByAuthorIdOrderedByUploadDateDesc(authorId: Int) = dbQuery {
        PostTable.selectAll()
            .where { PostTable.authorId eq authorId }
            .orderBy(PostTable.uploadDate to SortOrder.DESC)
            .map { it.toPostDto() }
            .toImmutableList()
    }

    override suspend fun updatePost(postDto: PostDto): Boolean
    {
        if (postDto.id == null)
            throw IllegalArgumentException("Post id must not be null when updating an existing post")

        return dbQuery {
            PostTable.update({ PostTable.id eq postDto.id }) {
                it[title] = postDto.title
                it[content] = postDto.content
                it[public] = postDto.public
                it[uploadDate] = postDto.uploadDate
            }
        } > 0
    }

    override suspend fun deletePost(id: Int): Boolean
    {
        commentService.deleteCommentsForPost(id)
        voteService.deleteVotesForPost(id)

        return dbQuery {
            PostTable.deleteWhere { PostTable.id eq id } > 0
        }
    }
}
