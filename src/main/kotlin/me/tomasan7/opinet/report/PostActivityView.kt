package me.tomasan7.opinet.report

import me.tomasan7.opinet.comment.CommentTable
import me.tomasan7.opinet.database.View
import me.tomasan7.opinet.post.PostTable
import me.tomasan7.opinet.vote.VoteTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.countDistinct

/*
SELECT POST.ID, COUNT(DISTINCT VOTE.ID) + COUNT(DISTINCT COMMENT.ID) AS activity
FROM POST
    LEFT JOIN VOTE ON POST.ID = VOTE.POST_ID
    LEFT JOIN COMMENT ON POST.ID = COMMENT.POST_ID
GROUP BY POST.ID;
*/

private const val ACTIVITY_SUM_ALIAS = "activity"
private val ACTIVITY_SUM_EXPR = Expression.build {
    (VoteTable.id.countDistinct() + CommentTable.id.countDistinct()).alias(ACTIVITY_SUM_ALIAS)
}

/**
 * Post id to (upvotes + downvotes + comments).
 */
object PostActivityView : View(
    name = "post_activity",
    select = (PostTable leftJoin VoteTable leftJoin CommentTable)
        .select(PostTable.id, ACTIVITY_SUM_EXPR)
        .groupBy(PostTable.id)
)
{
    val postId = column(PostTable.id)
    val activity = registerColumn(ACTIVITY_SUM_ALIAS, IntegerColumnType())
}
