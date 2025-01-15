package me.tomasan7.opinet.user

import diglol.crypto.Hash
import me.tomasan7.opinet.service.DatabaseService
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import java.sql.SQLIntegrityConstraintViolationException

class DatabaseUserService(
    database: Database
) : UserService, DatabaseService(database, UserTable)
{
    /* TODO: Replace with Argon2 */
    private val sha256 = Hash(Hash.Type.SHA256)
    override suspend fun createUser(userDto: UserDto, password: String): Int
    {
        if (userDto.id != null)
            throw IllegalArgumentException("User id must be null when creating a new user")

        val passwordHash = sha256.hash(password.toByteArray(Charsets.UTF_8))

        return dbQuery {
            try
            {
                UserTable.insertAndGetId {
                    it[username] = userDto.username
                    it[firstName] = userDto.firstName
                    it[lastName] = userDto.lastName
                    it[gender] = userDto.gender
                    it[this.password] = passwordHash
                }.value
            }
            catch (e: ExposedSQLException)
            {
                if (e.cause is SQLIntegrityConstraintViolationException)
                    throw UsernameAlreadyExistsException(userDto.username)
                else
                    throw e
            }
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

    override suspend fun getAllUsers(): List<UserDto>
    {
        return dbQuery {
            UserTable.selectAll()
                .map { it.toUser() }
        }
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
