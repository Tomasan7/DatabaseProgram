package me.tomasan7.opinet.user

/** Manages users. */
interface UserService
{
    /**
     * Creates a new user with [password].
     *
     * @return The user's new id.
     * @throws UsernameAlreadyExistsException
     */
    suspend fun createUser(userDto: UserDto, password: String): Int

    /** Returns a [user][UserDto] with [id]. Or `null` if not found. */
    suspend fun getUserById(id: Int): UserDto?

    suspend fun getAllUsers(): List<UserDto>

    /** Returns a [user][UserDto] with [username]. Or `null` if not found. */
    suspend fun getUserByUsername(username: String): UserDto?

    /** Returns `true` if the user with [username] exists and his password matches with [password]. */
    suspend fun loginUser(username: String, password: String): Boolean
}

class UsernameAlreadyExistsException(username: String) : Exception("Username '$username' already exists")
