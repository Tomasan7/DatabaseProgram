package me.tomasan7.opinet.vote

import me.tomasan7.opinet.database.View
import me.tomasan7.opinet.post.PostTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.booleanLiteral
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.sum


/*
SELECT POST.ID, SUM(CASE WHEN VOTE.UP_DOWN = TRUE THEN 1 WHEN VOTE.UP_DOWN = FALSE THEN -1 ELSE 0 END) AS votes
FROM POST
    LEFT JOIN VOTE ON POST.ID = VOTE.POST_ID
GROUP BY POST.ID
*/

private const val VOTE_SUM_ALIAS = "votes"
private val VOTE_SUM_EXPR = Expression.Companion.build {
    case()
        .When(VoteTable.upDown eq booleanLiteral(true), intLiteral(1))
        .When(VoteTable.upDown eq booleanLiteral(false), intLiteral(-1))
        .Else(intLiteral(0))
        .sum()
        .alias(VOTE_SUM_ALIAS)
}

/**
 * Post id to (upvotes - downvotes).
 */
object PostAbsoluteVotesView : View(
    name = "post_absolute_votes",
    select = (PostTable leftJoin VoteTable)
        .select(PostTable.id, VOTE_SUM_EXPR)
        .groupBy(PostTable.id)
)
{
    val postId = column(PostTable.id)
    val votes = registerColumn(VOTE_SUM_ALIAS, IntegerColumnType())
}
