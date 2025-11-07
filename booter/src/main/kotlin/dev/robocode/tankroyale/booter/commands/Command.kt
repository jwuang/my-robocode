package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BootEntry
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name

abstract class Command {

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
        }
    }

    protected fun getBootEntry(botDirPath: Path): BootEntry? {
        // 寻找目录下唯一的 json 文件（按你约定就是 bot 的配置文件）
        val jsonFile = botDirPath.toFile().listFiles()
            ?.firstOrNull { it.isFile && it.extension == "json" }
            ?: return null

        val bootEntryJsonContent = jsonFile.readText(Charsets.UTF_8)
        return json.decodeFromString(bootEntryJsonContent)
    }
}