package me.tomasan7.opinet.user

import diglol.crypto.Hash
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.SQLIntegrityConstraintViolationException

class DatabaseUserService(
    private val database: Database
) : UserService
{
    /* TODO: Replace with Argon2 */
    private val sha256 = Hash(Hash.Type.SHA256)

    private suspend fun <T> dbQuery(statement: Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, database, statement = statement)

    suspend fun init() = dbQuery {
        SchemaUtils.create(UserTable)
    }

    private fun ResultRow.toUser() = UserDto(
        username = this[UserTable.username],
        firstName = this[UserTable.firstName],
        lastName = this[UserTable.lastName],
        gender = this[UserTable.gender],
        id = this[UserTable.id].value
    )

    override suspend fun createUser(userDto: UserDto, password: String): Int
    {
        if (userDto.id != null)
            throw IllegalArgumentException("User id must be null when creating a new user")

        val passwordHash = sha256.hash(password.toByteArray(Charsets.UTF_8))

        return try
        {
            dbQuery {
                UserTable.insertAndGetId {
                    it[username] = userDto.username
                    it[firstName] = userDto.firstName
                    it[lastName] = userDto.lastName
                    it[gender] = userDto.gender
                    it[this.password] = passwordHash
                }.value
            }
        }
        catch (e: ExposedSQLException)
        {
            if (e.cause is SQLIntegrityConstraintViolationException)
                throw UsernameAlreadyExistsException(userDto.username)
            else
                throw e
        }
    }

    override suspend fun getUserById(id: Int) = dbQuery {
        UserTable.selectAll()
            .where { UserTable.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun getUserByUsername(username: String) = dbQuery {
        UserTable.selectAll()
            .where { UserTable.username eq username }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun loginUser(username: String, password: String): Boolean
    {
        val passwordHash = sha256.hash(password.toByteArray(Charsets.UTF_8))

        return dbQuery {
            UserTable.selectAll()
                .where { (UserTable.username eq username) and (UserTable.password eq passwordHash) }
                .singleOrNull()
                ?.let { it[UserTable.password] contentEquals passwordHash }
                ?: false
        }
    }
}
