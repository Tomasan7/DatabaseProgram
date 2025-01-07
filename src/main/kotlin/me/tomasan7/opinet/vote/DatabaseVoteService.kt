package me.tomasan7.opinet.vote

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DatabaseVoteService(
    private val database: Database
) : VoteService
{
    private suspend fun <T> dbQuery(statement: Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, database, statement = statement)

    suspend fun init() = dbQuery {
        SchemaUtils.create(VoteTable)
    }

    private fun ResultRow.toVoteDto() = VoteDto(
        upDown = this[VoteTable.upDown],
        votedAt = Instant.fromEpochSeconds(this[VoteTable.votedAt].toLong()),
        userId = this[VoteTable.userId].value,
        postId = this[VoteTable.postId].value,
        id = this[VoteTable.id].value
    )

    override suspend fun createVote(voteDto: VoteDto): Int = dbQuery {
        VoteTable.insertAndGetId {
            it[upDown] = voteDto.upDown
            it[votedAt] = voteDto.votedAt.epochSeconds.toUInt()
            it[userId] = voteDto.userId
            it[postId] = voteDto.postId
        }.value
    }

    override suspend fun getVotesOnPost(postId: Int): ImmutableList<VoteDto> = dbQuery {
        VoteTable.selectAll()
            .where { VoteTable.postId eq postId }
            .map { it.toVoteDto() }
            .toImmutableList()
    }

    override suspend fun getPostsOrderedByVotes(): ImmutableList<Int> = dbQuery {
        VoteTable.select(VoteTable.postId)
            .groupBy(VoteTable.postId)
            .orderBy(VoteTable.postId.count() to SortOrder.DESC)
            .map { it[VoteTable.postId].value }
            .toImmutableList()
    }

    override suspend fun removeVoteByUserOnPost(userId: Int, postId: Int): Boolean = dbQuery {
        VoteTable.deleteWhere { (VoteTable.userId eq userId) and (VoteTable.postId eq postId) } > 0
    }

    override suspend fun deleteVotesForPost(postId: Int): Unit = dbQuery {
        VoteTable.deleteWhere { VoteTable.postId eq postId }
    }
}
