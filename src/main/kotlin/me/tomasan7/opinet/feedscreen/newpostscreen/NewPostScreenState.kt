package me.tomasan7.opinet.feedscreen.newpostscreen

data class NewPostScreenState(
    val maxLengths: MaxLengths,
    val isEditing: Boolean = false,
    val title: String = "",
    val content: String = "",
    val public: Boolean = false,
    val errorText: String? = null,
    val goBackToFeedEvent: Boolean = false
)
{
    data class MaxLengths(
        val title: Int,
        val content: Int
    )
}
