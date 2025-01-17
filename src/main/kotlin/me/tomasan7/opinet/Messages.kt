package me.tomasan7.opinet

object Messages
{
    const val networkError = "There was an error connecting to the database, check your internet connection"

    interface Import
    {
        val totalSuccess: String
        val partialSuccess: String

        val partialSuccessAborted: String

        val totalFailure: String
        val failureAborted: String

        companion object : Import
        {
            override val totalSuccess = "Successfully imported all %d {entities}."
            override val partialSuccess = "Successfully imported %d {entities}, but %d {entities} failed, check console for details."
            override val partialSuccessAborted = "Successfully imported %d {entities}, but %d {entities} failed and the import aborted on line %d, check console for details."
            override val totalFailure = "Failed to import all %d {entities}, check console for details."
            override val failureAborted = "Failed to import %d {entities}, and the import aborted on line %d, check console for details."
        }

        object Users : Import
        {
            override val totalSuccess = Import.totalSuccess.replace("{entities}", "users")
            override val partialSuccess = Import.partialSuccess.replace("{entities}", "users")
            override val partialSuccessAborted = Import.partialSuccessAborted.replace("{entities}", "users")
            override val totalFailure = Import.totalFailure.replace("{entities}", "users")
            override val failureAborted = Import.failureAborted.replace("{entities}", "users")
        }

        object Posts : Import
        {
            override val totalSuccess = Import.totalSuccess.replace("{entities}", "posts")
            override val partialSuccess = Import.partialSuccess.replace("{entities}", "posts")
            override val partialSuccessAborted = Import.partialSuccessAborted.replace("{entities}", "posts")
            override val totalFailure = Import.totalFailure.replace("{entities}", "posts")
            override val failureAborted = Import.failureAborted.replace("{entities}", "posts")
        }
    }
}
