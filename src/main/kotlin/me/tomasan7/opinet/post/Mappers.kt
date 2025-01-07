package me.tomasan7.opinet.post

import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toPostDto() = PostDto(
    title = this[PostTable.title],
    content = this[PostTable.content],
    authorId = this[PostTable.authorId].value,
    uploadDate = this[PostTable.uploadDate],
    id = this[PostTable.id].value
)
