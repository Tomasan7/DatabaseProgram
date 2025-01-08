package me.tomasan7.opinet.managementscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import me.tomasan7.opinet.config.Config
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.post.PostService
import me.tomasan7.opinet.report.ReportService
import me.tomasan7.opinet.user.Gender
import me.tomasan7.opinet.user.UserDto
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.user.UsernameAlreadyExistsException
import me.tomasan7.opinet.util.now
import me.tomasan7.opinet.util.parseLocalDate
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.pathString
import kotlin.sequences.forEach

private val logger = logger { }

class ManagementScreenModel(
    val importConfig: Config.Import,
    val userService: UserService,
    val postService: PostService,
    val reportService: ReportService
) : ScreenModel
{
    var uiState by mutableStateOf(ManagementScreenState())
        private set

    private val json = Json {
        prettyPrint = true
    }

    private var loadTotalReportJob: Job? = null

    init
    {
        loadTotalReport()
    }

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

    private fun loadTotalReport()
    {
        if (loadTotalReportJob?.isActive == true)
            return

        loadTotalReportJob = screenModelScope.launch {
            val mostActiveUserDef = async { reportService.getMostActiveUser() }
            val mostActivePostDef = async { reportService.getMostActivePost() }
            val mostUpvotedPostDef = async { reportService.getMostUpvotedPost() }
            val mostDownvotedPostDef = async { reportService.getMostDownvotedPost() }
            val mostCommentedPostDef = async { reportService.getMostCommentedPost() }

            awaitAll(
                mostActiveUserDef,
                mostActivePostDef,
                mostUpvotedPostDef,
                mostDownvotedPostDef,
                mostCommentedPostDef
            )

            val totalReport = TotalReport(
                mostActiveUser = mostActiveUserDef.await(),
                mostActivePost = mostActivePostDef.await(),
                mostUpvotedPost = mostUpvotedPostDef.await(),
                mostDownvotedPost = mostDownvotedPostDef.await(),
                mostCommentedPost = mostCommentedPostDef.await()
            )

            uiState = uiState.copy(
                totalReport = totalReport
            )
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun onGenerateReportClicked()
    {
        screenModelScope.launch {
            if (uiState.totalReport == null)
            {
                if (loadTotalReportJob?.isActive != true)
                    loadTotalReport()

                loadTotalReportJob?.join()
            }

            val byteArrayStream = ByteArrayOutputStream()
            json.encodeToStream(uiState.totalReport, byteArrayStream)

            uiState = uiState.copy(
                exportTotalReportBytes = byteArrayStream.toByteArray()
            )
        }
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

    fun onReportExportFileSave(file: PlatformFile?)
    {
        uiState = uiState.copy(exportTotalReportBytes = null)
    }
}
