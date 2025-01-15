package me.tomasan7.opinet.vote

import kotlinx.collections.immutable.ImmutableList

interface VoteService
{
    /**
     * Creates a vote for a post.
     *
     * @return The id of the new vote.
     */
    suspend fun createVote(voteDto: VoteDto): Int

    /** Gets the votes on a post. */
    suspend fun getVotesOnPost(postId: Int): ImmutableList<VoteDto>

    /** Gets ids of posts with most votes overall. */
    suspend fun getPostsOrderedByVotes(): ImmutableList<Int>

    /** Removes a vote by a user on a post. */
    suspend fun removeVoteByUserOnPost(userId: Int, postId: Int): Boolean

    /** Deletes all votes for a post. */
    suspend fun deleteVotesForPost(postId: Int)
}
