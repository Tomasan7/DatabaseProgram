package me.tomasan7.opinet.managementscreen

import kotlinx.serialization.Serializable
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.report.IntReportDto
import me.tomasan7.opinet.user.UserDto

@Serializable
data class TotalReport(
    val mostActiveUser: IntReportDto<UserDto>?,
    val mostActivePost: IntReportDto<PostDto>?,
    val mostUpvotedPost: IntReportDto<PostDto>?,
    val mostDownvotedPost: IntReportDto<PostDto>?,
    val mostCommentedPost: IntReportDto<PostDto>?
)
