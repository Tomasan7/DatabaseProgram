package me.tomasan7.opinet.managementscreen

import androidx.compose.runtime.Immutable

@Immutable
data class ManagementScreenState(
    val importUsers: Boolean = false,
    val usersImportResult: ImportResult? = null,
    val importPosts: Boolean = false,
    val postsImportResult: ImportResult? = null,
    val totalReport: TotalReport? = null,
    val errorText: String? = null,
    val exportTotalReportBytes: ByteArray? = null
)
{
    data class ImportResult(
        val succeeded: Int,
        val failed: Int,
        val abortLine: Int? = null
    )
    {
        val aborted = abortLine != null
        val partialSuccess = failed != 0 && succeeded != 0 && !aborted
        val partialSuccessAborted = failed != 0 && succeeded != 0 && aborted
        val totalSuccess = succeeded != 0 && failed == 0 && !aborted
        val successAborted = succeeded != 0 && failed == 0 && aborted
        val totalFailure = succeeded == 0 && failed != 0 && !aborted
        val failureAborted = succeeded == 0 && failed != 0 && aborted
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ManagementScreenState

        if (importUsers != other.importUsers) return false
        if (importPosts != other.importPosts) return false
        if (usersImportResult != other.usersImportResult) return false
        if (postsImportResult != other.postsImportResult) return false
        if (totalReport != other.totalReport) return false
        if (errorText != other.errorText) return false
        if (!exportTotalReportBytes.contentEquals(other.exportTotalReportBytes)) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = importUsers.hashCode()
        result = 31 * result + importPosts.hashCode()
        result = 31 * result + (usersImportResult?.hashCode() ?: 0)
        result = 31 * result + (postsImportResult?.hashCode() ?: 0)
        result = 31 * result + (totalReport?.hashCode() ?: 0)
        result = 31 * result + (errorText?.hashCode() ?: 0)
        result = 31 * result + (exportTotalReportBytes?.contentHashCode() ?: 0)
        return result
    }
}
