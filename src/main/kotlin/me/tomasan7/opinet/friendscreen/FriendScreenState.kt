package me.tomasan7.opinet.friendscreen

import me.tomasan7.opinet.user.UserDto

data class FriendScreenState(
    val users: List<MaybeFriend> = emptyList(),
    val userSearch: String = "",
    val errorText: String? = null
)

data class MaybeFriend(
    val user: UserDto,
    val isFriend: Boolean,
    val incomingRequest: Boolean,
    val outgoingRequest: Boolean
)
