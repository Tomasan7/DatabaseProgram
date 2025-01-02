package me.tomasan7.opinet.config

import com.sksamuel.hoplite.Masked
import java.nio.file.Path

data class Config(
    val database: Database,
    val import: Import,
    val logLevel: String,
    val sessionFile: Path
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
