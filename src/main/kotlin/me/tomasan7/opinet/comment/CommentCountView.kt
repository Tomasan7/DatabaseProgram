package me.tomasan7.opinet.comment

import me.tomasan7.opinet.database.View
import me.tomasan7.opinet.post.PostTable
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.count

/*
SELECT POST.ID, COUNT(COMMENT.ID) AS comment_count
FROM POST
    LEFT JOIN COMMENT ON POST.ID = COMMENT.POST_ID
GROUP BY POST.ID;
*/

private const val COMMENT_COUNT_ALIAS = "comment_count"

/**
 * Post id to comment count.
 */
object CommentCountView : View(
    name = "comment_count",
    select = (PostTable leftJoin CommentTable)
        .select(PostTable.id, CommentTable.id.count().alias(COMMENT_COUNT_ALIAS))
        .groupBy(PostTable.id)
)
{
    val postId = column(PostTable.id)
    val commentCount = registerColumn(COMMENT_COUNT_ALIAS, IntegerColumnType())
}
