package me.tomasan7.opinet.managementscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.config.Config
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.post.PostService
import me.tomasan7.opinet.report.ReportService
import me.tomasan7.opinet.user.Gender
import me.tomasan7.opinet.user.UserDto
import me.tomasan7.opinet.user.UserService
import me.tomasan7.opinet.user.UsernameAlreadyExistsException
import me.tomasan7.opinet.util.isNetworkError
import me.tomasan7.opinet.util.now
import me.tomasan7.opinet.util.parseLocalDate
import java.io.ByteArrayOutputStream
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
        changeUiState(importUsers = true)
    }

    fun onImportUsersFilesChosen(files: Iterable<String>)
    {
        changeUiState(importUsers = false)

        screenModelScope.launch {
            var importedUsersCount = 0
            var failedUsersCount = 0
            for (path in files)
                try
                {
                    readCsvSequence(path) { fields ->
                        if (fields.size != 5)
                        {
                            logger.warn { "IMPORT: Skipped line because it had ${fields.size} fields instead of 5" }
                            throw CSVFieldNumDifferentException(5, -1, fields.size)
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
                            failedUsersCount++
                        }
                        catch (e: Exception)
                        {
                            if (e.isNetworkError())
                                changeUiState(errorText = Messages.networkError)
                            else if (e is CancellationException)
                                throw e
                            failedUsersCount++
                            logger.error { "IMPORT: $username - $firstName $lastName was not imported. (${e.message})" }
                        }
                    }
                    changeUiState(usersImportResult = ManagementScreenState.ImportResult(importedUsersCount, failedUsersCount))
                }
                catch (e: CancellationException)
                {
                    throw e
                }
                catch (e: Exception)
                {
                    changeUiState(errorText = Messages.incorrectFormat.format(getUsersFormatString()))
                }
        }
    }

    fun onImportPostsClicked()
    {
        changeUiState(importPosts = true)
    }

    fun onImportUsersDismissed()
    {
        changeUiState(importUsers = false)
    }

    private fun loadTotalReport()
    {
        if (loadTotalReportJob?.isActive == true)
            return

        loadTotalReportJob = screenModelScope.launch {
            val supJob = SupervisorJob()
            val mostActiveUserDef = async(supJob) { reportService.getMostActiveUser() }
            val mostActivePostDef = async(supJob) { reportService.getMostActivePost() }
            val mostUpvotedPostDef = async(supJob) { reportService.getMostUpvotedPost() }
            val mostDownvotedPostDef = async(supJob) { reportService.getMostDownvotedPost() }
            val mostCommentedPostDef = async(supJob) { reportService.getMostCommentedPost() }

            try
            {
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

                changeUiState(totalReport = totalReport)
            }
            catch (e: Exception)
            {
                if (e.isNetworkError())
                    changeUiState(errorText = Messages.networkError)
                else if (e is CancellationException)
                    throw e
            }
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

            changeUiState(exportTotalReportBytes = byteArrayStream.toByteArray())
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
            var importedPostsCount = 0
            var failedPostsCount = 0
            for (path in paths)
                try
                {
                    readCsvSequence(path) { fields ->
                        if (fields.size != 5)
                        {
                            logger.info { "IMPORT: Post was not imported, because it has ${fields.size} fields instead of 5" }
                            throw CSVFieldNumDifferentException(5, -1, fields.size)
                        }

                        val (authorUsername, publicStr, uploadDateStr, title, content) = fields

                        if (authorUsername.isBlank() || uploadDateStr.isBlank() || title.isBlank() || content.isBlank())
                            return@readCsvSequence logger.info { "IMPORT: Post was not imported, because it has empty fields" }

                        val uploadDate = try
                        {
                            uploadDateStr.parseLocalDate(dateFormatter)
                        }
                        catch (e: Exception)
                        {
                            logger.info { "IMPORT: Post was not imported, because it has an invalid upload date format. dd.MM.yyyy is expected." }
                            return@readCsvSequence
                        }

                        if (uploadDate > LocalDate.now())
                            return@readCsvSequence logger.info { "IMPORT: Post was not imported, because it has an upload date in the future" }

                        val author = userService.getUserByUsername(authorUsername)

                        if (author == null)
                            return@readCsvSequence logger.info { "IMPORT: Post was not imported, because user '$authorUsername' does not exist" }

                        val postDto = PostDto(
                            title = title,
                            content = content,
                            uploadDate = uploadDate,
                            public = publicStr.toBoolean(),
                            authorId = author.id!!
                        )

                        try
                        {
                            postService.createPost(postDto)
                            importedPostsCount++
                            logger.info { "IMPORT: Imported post titled '$title' by $authorUsername uploaded at $uploadDate" }
                        }
                        catch (e: Exception)
                        {
                            if (e.isNetworkError())
                                changeUiState(errorText = Messages.networkError)
                            if (e is CancellationException)
                                throw e
                            logger.error { "IMPORT: Post titled '$title' was not imported. (${e.message})" }
                        }
                    }
                    changeUiState(postsImportResult = ManagementScreenState.ImportResult(importedPostsCount, failedPostsCount))
                }
                catch (e: Exception)
                {
                    if (e is CancellationException)
                        throw e
                    else
                        changeUiState(errorText = Messages.incorrectFormat.format(getPostsFormatString()))
                }
        }
    }

    private suspend fun readCsvSequence(
        path: String,
        action: suspend (List<String>) -> Unit
    )
    {
        csvReader {
            delimiter = importConfig.csvDelimiter
        }.openAsync(path) {
            readAllAsSequence().forEach { action(it) }
        }
    }

    fun getPostsFormatString(): String
    {
        return listOf(
            "authorName",
            "public(true/false)",
            "uploadDate(${importConfig.dateFormat})",
            "title",
            "content"
        ).joinToString(separator = importConfig.csvDelimiter.toString())
    }

    fun getUsersFormatString(): String
    {
        return listOf(
            "username",
            "firstName",
            "lastName",
            "password",
            "gender"
        ).joinToString(separator = importConfig.csvDelimiter.toString())
    }

    fun onErrorConsumed()
    {
        changeUiState(errorText = null)
    }

    fun onImportPostsDismissed()
    {
        changeUiState(importPosts = false)
    }

    fun onReportExportFileSave(file: PlatformFile?)
    {
        changeUiState(exportTotalReportBytes = null)
    }

    private fun changeUiState(
        importUsers: Boolean = uiState.importUsers,
        usersImportResult: ManagementScreenState.ImportResult? = uiState.usersImportResult,
        importPosts: Boolean = uiState.importPosts,
        postsImportResult: ManagementScreenState.ImportResult? = uiState.postsImportResult,
        totalReport: TotalReport? = uiState.totalReport,
        errorText: String? = uiState.errorText,
        exportTotalReportBytes: ByteArray? = uiState.exportTotalReportBytes
    )
    {
        uiState = ManagementScreenState(
            importUsers = importUsers,
            usersImportResult = usersImportResult,
            importPosts = importPosts,
            postsImportResult = postsImportResult,
            totalReport = totalReport,
            errorText = errorText,
            exportTotalReportBytes = exportTotalReportBytes
        )
    }
}
