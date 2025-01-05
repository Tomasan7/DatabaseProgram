package me.tomasan7.opinet.isolationlevels

sealed interface WriteTransactionAction<T>
{
    data class Write<T>(val value: T) : WriteTransactionAction<T>
    class Commit<T>() : WriteTransactionAction<T>
    class Rollback<T>() : WriteTransactionAction<T>
}
