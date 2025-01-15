package me.tomasan7.opinet.isolationlevels

data class DirtyWritesState(
    val transaction1InProgress: Boolean = false,
    val transaction2InProgress: Boolean = false,
    val updateValue1: String = "0",
    val updateValue2: String = "0",
    val expectedValue1: Float? = null,
    val expectedValue2: Float? = null,
    val actualValue1: Float? = null,
    val actualValue2: Float? = null,
    val actionHistory: List<String> = emptyList()
)
