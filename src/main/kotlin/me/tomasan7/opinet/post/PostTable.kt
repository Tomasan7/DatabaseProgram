package me.tomasan7.opinet.post

import me.tomasan7.opinet.user.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

object PostTable : IntIdTable("post")
{
    val title = varchar("title", 100)
    val content = text("content")
    val uploadDate = date("upload_date")
    val public = bool("public")
    val authorId = reference("author_id", UserTable)
}
