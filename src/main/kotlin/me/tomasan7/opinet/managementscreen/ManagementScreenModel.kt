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
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.config.Config
import me.tomasan7.opinet.post.PostDto
import me.tomasan7.opinet.post.PostService
import me.tomasan7.opinet.post.PostTable
import me.tomasan7.opinet.report.ReportService
import me.tomasan7.opinet.user.*
import me.tomasan7.opinet.util.isNetworkError
import me.tomasan7.opinet.util.now
import me.tomasan7.opinet.util.parseLocalDate
import me.tomasan7.opinet.util.size
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

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

    fun onImportUsersFilesChosen(file: String)
    {
        changeUiState(importUsers = false)

        screenModelScope.launch {
            var importedUsersCount = 0
            var failedUsersCount = 0
            var currentLineNum = 0
            try
            {
                readCsvSequence(file) { lineNum, fields ->
                    currentLineNum = lineNum
                    if (fields.size != 5)
                    {
                        failedUsersCount++
                        logger.warn { "IMPORT: Skipped line because it had ${fields.size} fields instead of 5" }
                        throw CSVFieldNumDifferentException(5, -1, fields.size)
                    }

                    val (username, firstName, lastName, password, genderStr) = fields

                    val gender = try
                    {
                        Gender.valueOf(genderStr)
                    }
                    catch (ignored: IllegalArgumentException)
                    {
                        failedUsersCount++
                        val possibleValues = Gender.entries.joinToString()
                        return@readCsvSequence logger.info { "IMPORT: User '$username' was not imported, because gender '$genderStr' does not exist. Possible values: $possibleValues" }
                    }

                    // TODO: ScreenModel should not directly depend on Model implementation. Move this logic to abstract service.
                    val maxUsernameLength = UserTable.username.size
                    val maxFirstNameLength = UserTable.firstName.size
                    val maxLastNameLength = UserTable.lastName.size
                    if (username.length > maxUsernameLength)
                    {
                        failedUsersCount++
                        return@readCsvSequence logger.info { "IMPORT: User '$username' was not imported, because username is too long. Max length is $maxUsernameLength" }
                    }
                    else if (firstName.length > maxFirstNameLength)
                    {
                        failedUsersCount++
                        return@readCsvSequence logger.info { "IMPORT: User '$username' was not imported, because first name is too long. Max length is $maxFirstNameLength" }
                    }
                    else if (lastName.length > maxLastNameLength)
                    {
                        failedUsersCount++
                        return@readCsvSequence logger.info { "IMPORT: User '$username' was not imported, because last name is too long. Max length is $maxLastNameLength" }
                    }


                    val userDto = UserDto(
                        username = username,
                        firstName = firstName,
                        lastName = lastName,
                        gender = gender
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
                changeUiState(
                    usersImportResult = ManagementScreenState.ImportResult(
                        importedUsersCount,
                        failedUsersCount
                    )
                )
            }
            catch (e: CancellationException)
            {
                throw e
            }
            catch (e: Exception)
            {
                val errorLine = currentLineNum + 1
                logger.error { "IMPORT: Import aborted on line $errorLine. ${e.message}" }
                changeUiState(
                    usersImportResult = ManagementScreenState.ImportResult(
                        importedUsersCount,
                        failedUsersCount,
                        errorLine
                    )
                )
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

    fun onImportPostsFilesChosen(file: String)
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
            var currentLineNum = 0
            try
            {
                readCsvSequence(file) { lineNum, fields ->
                    currentLineNum = lineNum
                    if (fields.size != 5)
                    {
                        failedPostsCount++
                        logger.info { "IMPORT: Post was not imported, because it has ${fields.size} fields instead of 5" }
                        throw CSVFieldNumDifferentException(5, -1, fields.size)
                    }

                    val (authorUsername, publicStr, uploadDateStr, title, content) = fields

                    if (authorUsername.isBlank() || uploadDateStr.isBlank() || title.isBlank() || content.isBlank())
                    {
                        failedPostsCount++
                        return@readCsvSequence logger.info { "IMPORT: Post was not imported, because it has empty fields" }
                    }

                    val uploadDate = try
                    {
                        uploadDateStr.parseLocalDate(dateFormatter)
                    }
                    catch (e: Exception)
                    {
                        failedPostsCount++
                        logger.info { "IMPORT: Post was not imported, because it has an invalid upload date format. dd.MM.yyyy is expected." }
                        return@readCsvSequence
                    }

                    if (uploadDate > LocalDate.now())
                    {
                        failedPostsCount++
                        return@readCsvSequence logger.info { "IMPORT: Post was not imported, because it has an upload date in the future" }
                    }

                    // TODO: ScreenModel should not directly depend on Model implementation. Move this logic to abstract service.
                    val maxTitleLength = PostTable.title.size
                    if (title.length > maxTitleLength)
                    {
                        failedPostsCount++
                        return@readCsvSequence logger.info { "IMPORT: Post was not imported, because title is too long. Max length is $maxTitleLength" }
                    }

                    try
                    {
                        val author = userService.getUserByUsername(authorUsername)

                        if (author == null)
                        {
                            failedPostsCount++
                            return@readCsvSequence logger.info { "IMPORT: Post was not imported, because user '$authorUsername' does not exist" }
                        }

                        val postDto = PostDto(
                            title = title,
                            content = content,
                            uploadDate = uploadDate,
                            public = publicStr.toBoolean(),
                            authorId = author.id!!
                        )
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
                changeUiState(
                    postsImportResult = ManagementScreenState.ImportResult(
                        importedPostsCount,
                        failedPostsCount
                    )
                )
            }
            catch (e: CancellationException)
            {
                throw e
            }
            catch (e: Exception)
            {
                val errorLine = currentLineNum + 1
                logger.error { "IMPORT: Import aborted on line $errorLine: ${e.message}" }
                changeUiState(
                    postsImportResult = ManagementScreenState.ImportResult(
                        importedPostsCount,
                        failedPostsCount,
                        errorLine
                    )
                )
            }
        }
    }

    private suspend fun readCsvSequence(
        path: String,
        action: suspend (Int, List<String>) -> Unit
    )
    {
        csvReader {
            delimiter = importConfig.csvDelimiter
        }.openAsync(path) {
            readAllAsSequence().withIndex().forEach { action(it.index + 1, it.value) }
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
