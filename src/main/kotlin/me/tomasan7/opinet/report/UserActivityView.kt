package me.tomasan7.opinet.report

import me.tomasan7.opinet.comment.CommentTable
import me.tomasan7.opinet.database.View
import me.tomasan7.opinet.post.PostTable
import me.tomasan7.opinet.user.UserTable
import me.tomasan7.opinet.vote.VoteTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.countDistinct

/*
SELECT U.ID, COUNT(DISTINCT VOTE.ID) + COUNT(DISTINCT COMMENT.ID) + COUNT(DISTINCT POST.ID) AS activity
FROM "user" U
         LEFT JOIN VOTE ON U.ID = VOTE.USER_ID
         LEFT JOIN COMMENT ON U.ID = COMMENT.AUTHOR_ID
         LEFT JOIN POST ON U.ID = POST.AUTHOR_ID
GROUP BY U.ID;
*/

private const val ACTIVITY_SUM_ALIAS = "activity"
private val ACTIVITY_SUM_EXPR = Expression.build {
    (VoteTable.id.countDistinct() + CommentTable.id.countDistinct() + PostTable.id.countDistinct()).alias(ACTIVITY_SUM_ALIAS)
}

/**
 * User id to (upvotes + downvotes + comments).
 */
object UserActivityView : View(
    name = "user_activity",
    select = (UserTable leftJoin VoteTable leftJoin CommentTable leftJoin PostTable)
        .select(UserTable.id, ACTIVITY_SUM_EXPR)
        .groupBy(UserTable.id)
)
{
    val userId = column(UserTable.id)
    val activity = registerColumn(ACTIVITY_SUM_ALIAS, IntegerColumnType())
}
