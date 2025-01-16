package me.tomasan7.opinet.config

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.hocon.HoconParser
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class FileConfigProvider(
    private val path: String
) : ConfigProvider
{
    @OptIn(ExperimentalHoplite::class)
    override fun getConfig(): Config
    {
        createFileIfNotExists()

        return ConfigLoaderBuilder.default()
            .addParser("conf", HoconParser())
            .addFileSource(File(path))
            .withExplicitSealedTypes()
            .addDecoder(CharDecoder)
            .addParameterMapper(KebabCaseParamMapper)
            .build()
            .loadConfigOrThrow()
    }

    private fun createFileIfNotExists(): Boolean
    {
        val defaultConfigResource = this::class.java.getResource(DEFAULT_CONFIG_RESOURCE_PATH)
            ?: throw IllegalStateException("Default config resource not found")

        val file = File(path)

        if (!file.exists())
        {
            logger.info { "Creating default config file at $path" }

            defaultConfigResource.openStream().use { defaultConfigInputStream ->
                Files.copy(defaultConfigInputStream, file.toPath())
            }

            return true
        }

        return false
    }

    companion object
    {
        const val DEFAULT_CONFIG_RESOURCE_PATH = "/config.conf"
    }
}
