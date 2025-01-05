package me.tomasan7.opinet.managementscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import me.tomasan7.opinet.config.Config
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.post.PostService
import me.tomasan7.opinet.user.Gender
import me.tomasan7.opinet.user.UserDto
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.user.UsernameAlreadyExistsException
import me.tomasan7.opinet.util.now
import me.tomasan7.opinet.util.parseLocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4
import kotlin.sequences.forEach

private val logger = logger { }

class ManagementScreenModel(
    val importConfig: Config.Import,
    val userService: UserService,
    val postService: PostService
) : ScreenModel
{
    var uiState by mutableStateOf(ManagementScreenState())
        private set

    fun onImportUsersClicked()
    {
        uiState = uiState.copy(
            importUsers = true
        )
    }

    fun onImportUsersFilesChosen(files: Iterable<String>)
    {
        uiState = uiState.copy(
            importUsers = false
        )

        screenModelScope.launch {
            var importedUsersCount = 0
            for (path in files)
                csvReader {
                    delimiter = importConfig.csvDelimiter
                }.openAsync(path) {
                    readAllAsSequence().forEach { fields ->
                        if (fields.size != 5)
                        {
                            logger.warn { "IMPORT: Skipped line because it had ${fields.size} fields instead of 3" }
                            return@forEach
                        }

                        val (username, firstName, lastName, password, genderStr) = fields

                        val userDto = UserDto(
                            username = username,
                            firstName = firstName,
                            lastName = lastName,
                            gender = Gender.valueOf(genderStr)
                        )
                        try
                        {
                            userService.createUser(userDto, password)
                            importedUsersCount++
                            logger.info { "IMPORT: Imported $username - $firstName $lastName" }
                        }
                        catch (e: UsernameAlreadyExistsException)
                        {
                            logger.info { "IMPORT: $username - $firstName $lastName was not imported, because it already exists" }
                        }
                        catch (e: Exception)
                        {
                            logger.error { "IMPORT: $username - $firstName $lastName was not imported. (${e.message})" }
                        }
                    }
                }

            uiState = uiState.copy(
                usersImportResult = importedUsersCount
            )
        }
    }

    fun onImportPostsClicked()
    {
        uiState = uiState.copy(
            importPosts = true
        )
    }

    fun onImportUsersDismissed()
    {
        uiState = uiState.copy(
            importUsers = false
        )
    }


    fun onImportPostsFilesChosen(paths: Iterable<String>)
    {
        val dateFormatter = try
        {
            DateTimeFormatter.ofPattern(importConfig.dateFormat)
        }
        catch (e: IllegalArgumentException)
        {
            logger.warn { "IMPORT: Configured date format is not valid (${importConfig.dateFormat}), aborting import..." }
            return
        }

        screenModelScope.launch {
            for (path in paths)
                csvReader {
                    delimiter = importConfig.csvDelimiter
                }.openAsync(path) {
                    readAllAsSequence().forEach { fields ->
                        if (fields.size != 4)
                        {
                            logger.info { "IMPORT: Post was not imported, because it has ${fields.size} fields instead of 4" }
                            return@forEach
                        }

                        val (authorUsername, uploadDateStr, title, content) = fields

                        if (authorUsername.isBlank() || uploadDateStr.isBlank() || title.isBlank() || content.isBlank())
                        {
                            logger.info { "IMPORT: Post was not imported, because it has empty fields" }
                            return@forEach
                        }

                        val uploadDate = try
                        {
                            uploadDateStr.parseLocalDate(dateFormatter)
                        }
                        catch (e: Exception)
                        {
                            logger.info { "IMPORT: Post was not imported, because it has an invalid upload date format. dd.MM.yyyy is expected." }
                            println(e)
                            return@forEach
                        }

                        if (uploadDate > LocalDate.now())
                        {
                            logger.info { "IMPORT: Post was not imported, because it has an upload date in the future" }
                            return@forEach
                        }

                        val author = userService.getUserByUsername(authorUsername)

                        if (author == null)
                        {
                            logger.info { "IMPORT: Post was not imported, because user '$authorUsername' does not exist" }
                            return@forEach
                        }

                        val postDto = PostDto(
                            title = title,
                            content = content,
                            uploadDate = uploadDate,
                            authorId = author.id!!
                        )

                        try
                        {
                            postService.createPost(postDto)
                            logger.info { "IMPORT: Imported post titled '$title' by $authorUsername uploaded at $uploadDate" }
                        }
                        catch (e: Exception)
                        {
                            logger.error { "IMPORT: Post titled '$title' was not imported. (${e.message})" }
                        }
                    }
                }
        }
    }

    fun onImportPostsDismissed()
    {
        uiState = uiState.copy(
            importPosts = false
        )
    }
}
