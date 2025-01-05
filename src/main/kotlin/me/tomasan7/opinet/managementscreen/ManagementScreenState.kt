package me.tomasan7.opinet.managementscreen

data class ManagementScreenState(
    val importUsers: Boolean = false,
    val usersImportResult: Int? = null,
    val importPosts: Boolean = false,
    val postsImportResult: Int? = null
)
