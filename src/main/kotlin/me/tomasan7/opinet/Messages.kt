package me.tomasan7.opinet

object Messages
{
    const val networkError = "There was an error connecting to the database, check your internet connection"
    const val incorrectFormat = "The file was in incorrect format. Expected '%s'"
    private const val partialImportSuccess = "Successfully imported %d {entity}, but %d {entity} failed, check console for details."
    val usersImportPartialSuccess = partialImportSuccess.replace("{entity}", "users")
    val postsImportPartialSuccess = partialImportSuccess.replace("{entity}", "posts")
    private const val totalImportSuccess = "Successfully imported %d {entity}."
    val usersImportTotalSuccess = totalImportSuccess.replace("{entity}", "users")
    val postsImportTotalSuccess = totalImportSuccess.replace("{entity}", "posts")
    private const val totalImportFailure = "Failed to import %d {entity}, check console for details."
    val usersImportTotalFailure = totalImportFailure.replace("{entity}", "users")
    val postsImportTotalFailure = totalImportFailure.replace("{entity}", "posts")
}
