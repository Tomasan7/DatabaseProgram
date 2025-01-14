package me.tomasan7.opinet.friend

import me.tomasan7.opinet.user.UserDto

interface FriendService
{
    /** Returns a list of users who have sent a friend request to [userId]. */
    suspend fun getIncomingRequestsFor(userId: Int): List<UserDto>

    /** Returns a list of users to whom [userId] has sent a friend request. */
    suspend fun getOutgoingRequestsFrom(userId: Int): List<UserDto>

    /** Returns friends of [userId]. */
    suspend fun getFriendsOf(userId: Int): List<UserDto>

    suspend fun removeFriendship(userId1: Int, userId2: Int)

    suspend fun requestFriendship(requesterUserId: Int, targetUserId: Int)

    suspend fun acceptRequest(requesterUserId: Int, targetUserId: Int)

    suspend fun rejectRequest(requesterUserId: Int, targetUserId: Int)
}
