package me.tomasan7.opinet.comment

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseCommentService(
    private val database: Database
) : CommentService
{
    private suspend fun <T> dbQuery(statement: Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, database, statement = statement)

    suspend fun init() = dbQuery {
        SchemaUtils.create(CommentTable)
    }

    private fun ResultRow.toCommentDto() = CommentDto(
        text = this[CommentTable.text],
        uploadDate = this[CommentTable.uploadDate],
        authorId = this[CommentTable.authorId].value,
        postId = this[CommentTable.postId].value,
        id = this[CommentTable.id].value
    )

    override suspend fun createComment(comment: CommentDto): Int
    {
        if (comment.id != null)
            throw IllegalArgumentException("Comment id must be null when creating a new comment")

        return dbQuery {
            CommentTable.insertAndGetId {
                it[text] = comment.text
                it[authorId] = comment.authorId
                it[uploadDate] = comment.uploadDate
                it[postId] = comment.postId
            }.value
        }
    }

    override suspend fun getCommentById(id: Int) = dbQuery {
        CommentTable.selectAll()
            .where { CommentTable.id eq id }
            .singleOrNull()
            ?.toCommentDto()
    }

    override suspend fun getAllCommentsForPostOrderedByUploadDateDesc(postId: Int) = dbQuery {
        CommentTable.selectAll()
            .where { CommentTable.postId eq postId }
            .orderBy(CommentTable.uploadDate to SortOrder.DESC)
            .map { it.toCommentDto() }
            .toImmutableList()
    }

    override suspend fun getAllCommentsForUser(authorId: Int) = dbQuery {
        CommentTable.selectAll()
            .where { CommentTable.authorId eq authorId }
            .map { it.toCommentDto() }
            .toImmutableList()
    }

    override suspend fun getNumberOfCommentsForPost(postId: Int) = dbQuery {
        CommentTable.select(CommentTable.id)
            .where { CommentTable.postId eq postId }
            .count()
    }

    override suspend fun deleteCommentsForPost(postId: Int) = dbQuery {
        CommentTable.deleteWhere { CommentTable.postId eq postId }
    }
}
