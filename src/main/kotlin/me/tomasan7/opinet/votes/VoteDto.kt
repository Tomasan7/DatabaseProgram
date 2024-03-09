package me.tomasan7.opinet.votes

import kotlinx.datetime.Instant

data class VoteDto(
    val upDown: Boolean,
    val votedAt: Instant,
    val userId: Int,
    val postId: Int,
    val id: Int? = null
)
