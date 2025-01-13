package me.tomasan7.opinet.friend

import kotlinx.datetime.Clock
import me.tomasan7.opinet.service.DatabaseService
import me.tomasan7.opinet.user.UserDto
import me.tomasan7.opinet.user.UserTable
import me.tomasan7.opinet.user.toUser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DatabaseFriendService(
    database: Database
) : FriendService, DatabaseService(database, FriendTable)
{
    override suspend fun getIncomingRequestsFor(userId: Int): List<UserDto>
    {
        val f1 = FriendTable.alias("f1")
        val f2 = FriendTable.alias("f2")

        return dbQuery {
            f1.leftJoin(
                otherTable = f2,
                additionalConstraint = {
                    (f1[FriendTable.requesterId] eq f2[FriendTable.targetId]) and
                            (f1[FriendTable.targetId] eq f2[FriendTable.requesterId])
                }
            ).select(f1[FriendTable.requesterId])
                .where {
                    (f1[FriendTable.targetId] eq userId) and (f2[FriendTable.requesterId].isNull())
                }
                .map { fRow ->
                    // OPTIMIZE: Use join instead
                    UserTable.selectAll().where { UserTable.id eq fRow[f1[FriendTable.requesterId]] }.limit(1).single().toUser()
                }
        }
    }

    override suspend fun getOutgoingRequestsFrom(userId: Int): List<UserDto>
    {
        val f1 = FriendTable.alias("f1")
        val f2 = FriendTable.alias("f2")

        return dbQuery {
            f1.leftJoin(
                otherTable = f2,
                additionalConstraint = {
                    (f1[FriendTable.requesterId] eq f2[FriendTable.targetId]) and
                            (f1[FriendTable.targetId] eq f2[FriendTable.requesterId])
                }
            ).select(f1[FriendTable.targetId])
                .where {
                    (f1[FriendTable.requesterId] eq userId) and (f2[FriendTable.requesterId].isNull())
                }
                .map { fRow ->
                    // OPTIMIZE: Use join instead
                    UserTable.selectAll().where { UserTable.id eq fRow[f1[FriendTable.targetId]] }.limit(1).single().toUser()
                }
        }
    }


    override suspend fun getFriendsOf(userId: Int): List<UserDto>
    {
        val f1 = FriendTable.alias("f1")
        val f2 = FriendTable.alias("f2")

        val caseExpression = Expression.build {
            case()
                .When(f1[FriendTable.requesterId] eq userId, f1[FriendTable.targetId])
                .Else(f1[FriendTable.requesterId])
                .alias("friend_id")
        }

        return dbQuery {
            f1.innerJoin(
                otherTable = f2,
                additionalConstraint = { (f1[FriendTable.requesterId] eq f2[FriendTable.targetId]) and (f1[FriendTable.targetId] eq f2[FriendTable.requesterId]) },
            ).select(caseExpression)
                .withDistinct()
                .where { (f1[FriendTable.requesterId] eq userId) or (f1[FriendTable.targetId] eq userId) }
                .map { fRow ->
                    // OPTIMIZE: Use join instead
                    UserTable.selectAll().where { UserTable.id eq fRow[caseExpression] }.limit(1).single().toUser()
                }
        }
    }

    override suspend fun requestFriendship(requesterUserId: Int, targetUserId: Int)
    {
        dbQuery {
            FriendTable.insert {
                it[requesterId] = requesterUserId
                it[targetId] = targetUserId
                it[createdAt] = Clock.System.now()
            }
        }
    }

    override suspend fun acceptRequest(requesterUserId: Int, targetUserId: Int)
    {
        dbQuery {
            FriendTable.insert {
                it[requesterId] = targetUserId
                it[targetId] = requesterUserId
                it[createdAt] = Clock.System.now()
            }
        }
    }

    override suspend fun rejectRequest(requesterUserId: Int, targetUserId: Int)
    {
        dbQuery {
            FriendTable
                .deleteWhere(limit = 1) { (FriendTable.requesterId eq requesterId) and (FriendTable.targetId eq targetUserId) }
        }
    }
}
