package me.tomasan7.opinet.post

import kotlinx.collections.immutable.ImmutableList

/** Manages posts. */
interface PostService
{
    /**
     * Creates a new post.
     *
     * @return The post's new id.
     */
    suspend fun createPost(postDto: PostDto): Int

    /** Returns a [post][PostDto] with [id]. Or `null` if not found. */
    suspend fun getPostById(id: Int): PostDto?

    /** Returns all posts ordered by upload date descending. */
    suspend fun getAllPostsOrderedByUploadDateDesc(): ImmutableList<PostDto>

    /** Returns all posts visible to [userId]. Including their own, private and public posts. */
    suspend fun getAllPostsVisibleToOrderedByUploadDateDesc(userId: Int): ImmutableList<PostDto>

    /** Returns all **private** posts visible to [userId]. Including their own. */
    suspend fun getPrivatePostsVisibleToOrderedByUploadDateDesc(userId: Int): ImmutableList<PostDto>

    /** Returns all posts by user with id [authorId] ordered by upload date descending. */
    suspend fun getPostsByAuthorIdOrderedByUploadDateDesc(authorId: Int): ImmutableList<PostDto>

    /** Updates a post using the id in [postDto]. */
    suspend fun updatePost(postDto: PostDto): Boolean

    /** Deletes a post with id [id]. */
    suspend fun deletePost(id: Int): Boolean
}
