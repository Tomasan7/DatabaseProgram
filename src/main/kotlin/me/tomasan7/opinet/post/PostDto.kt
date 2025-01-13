package me.tomasan7.opinet.post

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val title: String,
    val content: String,
    val uploadDate: LocalDate,
    val authorId: Int,
    val public: Boolean,
    val id: Int? = null
)
