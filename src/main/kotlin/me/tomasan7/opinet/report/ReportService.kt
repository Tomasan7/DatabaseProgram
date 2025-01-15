package me.tomasan7.opinet.report

import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.user.UserDto

interface ReportService
{
    /**
     * Returns the most active user.
     * When there are more users with the same activity, one of them is returned.
     * Returns `null` if there are no users.
     */
    suspend fun getMostActiveUser(): IntReportDto<UserDto>?

    /**
     * Returns the most active post.
     * When there are more posts with the same activity, one of them is returned.
     * Returns `null` if there are no posts.
     */
    suspend fun getMostActivePost(): IntReportDto<PostDto>?

    /**
     * Returns the most upvoted post.
     * When there are more posts with the same activity, one of them is returned.
     * Returns `null` if there are no posts.
     */
    suspend fun getMostUpvotedPost(): IntReportDto<PostDto>?

    /**
     * Returns the most downvoted post.
     * When there are more posts with the same activity, one of them is returned.
     * Returns `null` if there are no posts.
     */
    suspend fun getMostDownvotedPost(): IntReportDto<PostDto>?

    /**
     * Returns the post with the most comments.
     * When there are more posts with the same activity, one of them is returned.
     * Returns `null` if there are no posts.
     */
    suspend fun getMostCommentedPost(): IntReportDto<PostDto>?
}
