package me.tomasan7.opinet.isolationlevels

data class DirtyReadsState(
    val writeTransactionInProgress: Boolean = false,
    val readTransactionInProgress: Boolean = false,
    val balanceAddition: String = "0",
    val actionHistory: List<String> = emptyList(),
    val readBalance: Float = 0f,
)
