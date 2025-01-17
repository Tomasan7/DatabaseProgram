package me.tomasan7.opinet.managementscreen

import StackedSnackbarHost
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import me.tomasan7.opinet.Messages
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.ui.component.HorizontalSpacer
import me.tomasan7.opinet.ui.component.ScreenTitle
import me.tomasan7.opinet.ui.component.Tooltipped
import me.tomasan7.opinet.ui.component.VerticalSpacer
import rememberStackedSnackbarHostState

object ManagementScreen : Screen
{
    private fun readResolve(): Any = ManagementScreen

    @Composable
    override fun Content()
    {
        val navigator = LocalNavigator.currentOrThrow
        val opiNet = navigator.getOpiNet()
        val model = rememberScreenModel { ManagementScreenModel(opiNet.getConfig().import, opiNet.userService, opiNet.postService, opiNet.reportService) }
        val uiState = model.uiState

        val importFilePicker = rememberFilePickerLauncher(
            type = PickerType.File(listOf("csv")),
            initialDirectory = System.getProperty("user.dir"),
            mode = PickerMode.Single
        ) { file ->
            if (file != null)
            {
                val path = file.path!!

                if (uiState.importUsers)
                    model.onImportUsersFilesChosen(path)
                else if (uiState.importPosts)
                    model.onImportPostsFilesChosen(path)
            }
            else
            {
                if (uiState.importUsers)
                    model.onImportUsersDismissed()
                else if (uiState.importPosts)
                    model.onImportPostsDismissed()
            }
        }

        LaunchedEffect(uiState.importUsers || uiState.importPosts) {
            if (uiState.importUsers || uiState.importPosts)
                importFilePicker.launch()
        }

        val reportExportFilePicker = rememberFileSaverLauncher { file -> model.onReportExportFileSave(file) }

        LaunchedEffect(uiState.exportTotalReportBytes) {
            if (uiState.exportTotalReportBytes != null)
                reportExportFilePicker.launch(
                    initialDirectory = System.getProperty("user.dir"),
                    baseName = "total_report",
                    extension = "json",
                    bytes = uiState.exportTotalReportBytes
                )
        }

        val stackedSnackbarHostState = rememberStackedSnackbarHostState(
            maxStack = 1,
            animation = StackedSnackbarAnimation.Slide
        )

        fun String.formatFailImportResultMessage(importResult: ManagementScreenState.ImportResult) = format(
            importResult.failed,
            importResult.abortLine
        )

        fun String.formatSuccessImportResultMessage(importResult: ManagementScreenState.ImportResult) = format(
            importResult.succeeded,
            importResult.abortLine
        )

        fun String.formatPartialSuccessImportResultMessage(importResult: ManagementScreenState.ImportResult) = format(
            importResult.succeeded,
            importResult.failed,
            importResult.abortLine
        )

        LaunchedEffect(uiState.usersImportResult) {
            val usersImportResult = uiState.usersImportResult
            if (usersImportResult != null)
                when
                {
                    usersImportResult.totalSuccess -> stackedSnackbarHostState.showSuccessSnackbar(
                        title = Messages.Import.Users.totalSuccess.formatSuccessImportResultMessage(usersImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                    usersImportResult.successAborted -> stackedSnackbarHostState.showWarningSnackbar(
                        title = Messages.Import.Users.partialSuccessAborted.formatSuccessImportResultMessage(usersImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                    usersImportResult.partialSuccess -> stackedSnackbarHostState.showWarningSnackbar(
                        title = Messages.Import.Users.partialSuccess.formatPartialSuccessImportResultMessage(usersImportResult),
                        duration = StackedSnackbarDuration.Long
                    )
                    usersImportResult.partialSuccessAborted -> stackedSnackbarHostState.showWarningSnackbar(
                        title = Messages.Import.Users.partialSuccessAborted.formatPartialSuccessImportResultMessage(usersImportResult),
                        duration = StackedSnackbarDuration.Long
                    )
                    usersImportResult.totalFailure -> stackedSnackbarHostState.showErrorSnackbar(
                        title = Messages.Import.Users.totalFailure.formatFailImportResultMessage(usersImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                    usersImportResult.failureAborted -> stackedSnackbarHostState.showErrorSnackbar(
                        title = Messages.Import.Users.failureAborted.formatFailImportResultMessage(usersImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                }

        }
        LaunchedEffect(uiState.postsImportResult) {
            val postsImportResult = uiState.postsImportResult
            if (postsImportResult != null)
                when
                {
                    postsImportResult.totalSuccess -> stackedSnackbarHostState.showSuccessSnackbar(
                        title = Messages.Import.Posts.totalSuccess.formatSuccessImportResultMessage(postsImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                    postsImportResult.successAborted -> stackedSnackbarHostState.showWarningSnackbar(
                        title = Messages.Import.Posts.partialSuccessAborted.formatSuccessImportResultMessage(postsImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                    postsImportResult.partialSuccess -> stackedSnackbarHostState.showWarningSnackbar(
                        title = Messages.Import.Posts.partialSuccess.formatPartialSuccessImportResultMessage(postsImportResult),
                        duration = StackedSnackbarDuration.Long
                    )
                    postsImportResult.partialSuccessAborted -> stackedSnackbarHostState.showWarningSnackbar(
                        title = Messages.Import.Posts.partialSuccessAborted.formatPartialSuccessImportResultMessage(postsImportResult),
                        duration = StackedSnackbarDuration.Long
                    )
                    postsImportResult.totalFailure -> stackedSnackbarHostState.showErrorSnackbar(
                        title = Messages.Import.Posts.totalFailure.formatFailImportResultMessage(postsImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                    postsImportResult.failureAborted -> stackedSnackbarHostState.showErrorSnackbar(
                        title = Messages.Import.Posts.failureAborted.formatFailImportResultMessage(postsImportResult),
                        duration = StackedSnackbarDuration.Short
                    )
                }
        }

        LaunchedEffect(uiState.errorText) {
            if (uiState.errorText != null)
            {
                stackedSnackbarHostState.showErrorSnackbar(
                    uiState.errorText,
                    duration = StackedSnackbarDuration.Short
                )
                model.onErrorConsumed()
            }
        }

        Scaffold(
            snackbarHost = { StackedSnackbarHost(stackedSnackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
            ) {
                ScreenTitle(
                    title = "Management",
                    onBackClick = { navigator.pop() }
                )
                VerticalSpacer(64.dp)
                Text(
                    text = "Import",
                    style = MaterialTheme.typography.headlineMedium
                )
                VerticalSpacer(24.dp)
                val localUriHandler = LocalUriHandler.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Tooltipped(model.getUsersFormatString()) {
                        Button({ model.onImportUsersClicked() }) {
                            Text("Import users")
                        }
                    }
                    IconButton(
                        onClick = { localUriHandler.openUri("https://github.com/tomhula/OpiNet?tab=readme-ov-file#users") }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Import users help"
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Tooltipped(model.getPostsFormatString()) {
                        Button({ model.onImportPostsClicked() }) {
                            Text("Import posts")
                        }
                    }
                    IconButton(
                        onClick = { localUriHandler.openUri("https://github.com/tomhula/OpiNet?tab=readme-ov-file#posts") }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Import posts info"
                        )
                    }
                }
                VerticalSpacer(64.dp)
                Text(
                    text = "Reports",
                    style = MaterialTheme.typography.headlineMedium
                )
                VerticalSpacer(24.dp)
                TotalReport(
                    totalReport = uiState.totalReport,
                    modifier = Modifier.widthIn(500.dp, 700.dp)
                )
                VerticalSpacer(24.dp)
                Button(
                    onClick = { model.onGenerateReportClicked() }
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export")
                    HorizontalSpacer(8.dp)
                    Text("Export report")
                }
            }
        }
    }

    @Composable
    private fun TotalReport(totalReport: TotalReport?, modifier: Modifier = Modifier)
    {
        val headerColumnWidth = 200.dp
        val valueColumnWidth = 100.dp

        Column(
            modifier = modifier
        ) {
            CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)) {
                TableRow(
                    header = { Text("Report") },
                    headerColumnWidth = headerColumnWidth,
                    entity = { Text("Entity") },
                    value = { Text("Value") },
                    valueColumnWidth = valueColumnWidth
                )
            }
            TableRow(
                header = { Text("Most active user") },
                headerColumnWidth = headerColumnWidth,
                entity = { Text(totalReport?.mostActiveUser?.entity?.let { "${it.firstName} ${it.lastName}" } ?: "...") },
                value = { Text(totalReport?.mostActiveUser?.value?.toString() ?: "...") },
                valueColumnWidth = valueColumnWidth
            )
            TableRow(
                header = { Text("Most active post") },
                headerColumnWidth = headerColumnWidth,
                entity = { Text(totalReport?.mostActivePost?.entity?.title ?: "...") },
                value = { Text(totalReport?.mostActivePost?.value?.toString() ?: "...") },
                valueColumnWidth = valueColumnWidth
            )
            TableRow(
                header = { Text("Most upvoted post") },
                headerColumnWidth = headerColumnWidth,
                entity = { Text(totalReport?.mostUpvotedPost?.entity?.title ?: "...") },
                value = { Text(totalReport?.mostUpvotedPost?.value?.toString() ?: "...") },
                valueColumnWidth = valueColumnWidth
            )
            TableRow(
                header = { Text("Most downvoted post") },
                headerColumnWidth = headerColumnWidth,
                entity = { Text(totalReport?.mostDownvotedPost?.entity?.title ?: "...") },
                value = { Text(totalReport?.mostDownvotedPost?.value?.toString() ?: "...") },
                valueColumnWidth = valueColumnWidth
            )
            TableRow(
                header = { Text("Most commented post") },
                headerColumnWidth = headerColumnWidth,
                entity = { Text(totalReport?.mostCommentedPost?.entity?.title ?: "...") },
                value = { Text(totalReport?.mostCommentedPost?.value?.toString() ?: "...") },
                valueColumnWidth = valueColumnWidth
            )
        }
    }

    @Composable
    private fun TableRow(
        header: @Composable BoxScope.() -> Unit,
        headerColumnWidth: Dp,
        entity: @Composable BoxScope.() -> Unit,
        value: @Composable BoxScope.() -> Unit,
        valueColumnWidth: Dp
    )
    {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
        ) {
            CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)) {
                TableCell(Modifier.fillMaxHeight().width(headerColumnWidth), header)
            }
            TableCell(Modifier.fillMaxHeight().weight(1f), entity)
            TableCell(Modifier.fillMaxHeight().width(valueColumnWidth), value)
        }
    }

    @Composable
    private fun TableCell(modifier: Modifier, text: @Composable BoxScope.() -> Unit)
    {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .border(Dp.Hairline, Color.Black),
            content = text
        )
    }
}
