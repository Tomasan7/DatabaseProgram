package me.tomasan7.opinet.managementscreen

import androidx.compose.runtime.Immutable

@Immutable
data class ManagementScreenState(
    val importUsers: Boolean = false,
    val usersImportResult: Int? = null,
    val importPosts: Boolean = false,
    val postsImportResult: Int? = null,
    val totalReport: TotalReport? = null,
    val errorText: String? = null,
    val exportTotalReportBytes: ByteArray? = null
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ManagementScreenState

        if (importUsers != other.importUsers) return false
        if (usersImportResult != other.usersImportResult) return false
        if (importPosts != other.importPosts) return false
        if (postsImportResult != other.postsImportResult) return false
        if (totalReport != other.totalReport) return false
        if (errorText != other.errorText) return false
        if (!exportTotalReportBytes.contentEquals(other.exportTotalReportBytes)) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = importUsers.hashCode()
        result = 31 * result + (usersImportResult ?: 0)
        result = 31 * result + importPosts.hashCode()
        result = 31 * result + (postsImportResult ?: 0)
        result = 31 * result + (totalReport?.hashCode() ?: 0)
        result = 31 * result + (errorText?.hashCode() ?: 0)
        result = 31 * result + (exportTotalReportBytes?.contentHashCode() ?: 0)
        return result
    }
}
