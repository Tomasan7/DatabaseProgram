package me.tomasan7.opinet.post

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import me.tomasan7.opinet.comment.CommentService
import me.tomasan7.opinet.vote.VoteService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DatabasePostService(
    private val database: Database,
    private val commentService: CommentService,
    private val voteService: VoteService
) : PostService
{
    private suspend fun <T> dbQuery(statement: Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, database, statement = statement)

    suspend fun init() = dbQuery {
        SchemaUtils.create(PostTable)
    }

    override suspend fun createPost(postDto: PostDto): Int
    {
        if (postDto.id != null)
            throw IllegalArgumentException("Post id must be null when creating a new post")

        return dbQuery {
            PostTable.insertAndGetId {
                it[title] = postDto.title
                it[content] = postDto.content
                it[uploadDate] = postDto.uploadDate
                it[authorId] = postDto.authorId
            }.value
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
