package me.tomasan7.opinet.config

import com.sksamuel.hoplite.Masked
import java.nio.file.Path

data class Config(
    val database: Database,
    val import: Import,
    val logLevel: String,
    val sessionFile: Path,
    val isolationLevel: IsolationLevel
)
{
    data class Database(
        val url: String,
        val driver: String,
        val user: String,
        val password: Masked
    )

    data class Import(
        val csvDelimiter: Char,
        val dateFormat: String
    )
}

enum class IsolationLevel(val id: Int)
{
    READ_UNCOMMITTED(1),
    READ_COMMITTED(2),
    REPEATABLE_READ(4),
    SERIALIZABLE(8)
}
