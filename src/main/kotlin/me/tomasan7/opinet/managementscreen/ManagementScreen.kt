package me.tomasan7.opinet.managementscreen

import StackedSnackbarHost
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import me.tomasan7.opinet.getOpiNet
import me.tomasan7.opinet.ui.component.HorizontalSpacer
import me.tomasan7.opinet.ui.component.VerticalSpacer
import rememberStackedSnackbarHostState
import kotlin.io.path.Path
import kotlin.io.path.absolute

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
            initialDirectory = Path(".").absolute().toString(),
            mode = PickerMode.Multiple(maxItems = 10)
        ) { files ->
            if (files != null)
            {
                val paths = files.map { it.path!! }

                if (uiState.importUsers)
                {
                    model.onImportUsersFilesChosen(paths)
                }
                else if (uiState.importPosts)
                    model.onImportPostsFilesChosen(paths)
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
                    initialDirectory = Path(".").absolute().toString(),
                    baseName = "total_report",
                    extension = "json",
                    bytes = uiState.exportTotalReportBytes
                )
        }

        val stackedSnackbarHostState = rememberStackedSnackbarHostState(
            maxStack = 1,
            animation = StackedSnackbarAnimation.Slide
        )

        LaunchedEffect(uiState.usersImportResult) {
            if (uiState.usersImportResult != null)
                stackedSnackbarHostState.showSuccessSnackbar(
                    title = "Imported ${uiState.usersImportResult} users",
                    duration = StackedSnackbarDuration.Short
                )
        }
        LaunchedEffect(uiState.postsImportResult) {
            if (uiState.postsImportResult != null)
                stackedSnackbarHostState.showSuccessSnackbar(
                    title = "Imported ${uiState.postsImportResult} posts",
                    duration = StackedSnackbarDuration.Short
                )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navigator.pop() }
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }

                    Text(
                        text = "Management",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                VerticalSpacer(64.dp)
                Text(
                    text = "Import",
                    style = MaterialTheme.typography.headlineMedium
                )
                VerticalSpacer(24.dp)
                Button(
                    onClick = { model.onImportUsersClicked() }
                ) {
                    Text("Import users")
                }
                Button(
                    onClick = { model.onImportPostsClicked() }
                ) {
                    Text("Import posts")
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
