package net.turtton.shuttle.dependency

import java.io.File
import java.util.jar.JarFile
import kotlin.io.path.createTempFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger

private val json = Json {
    ignoreUnknownKeys = true
}

@Serializable
private data class JarFileEntry(val file: String)

// https://json.schemastore.org/fabric.mod.json
@Serializable
private data class MiniFabricModJson(val id: String, val version: String, val jars: List<JarFileEntry>? = null)

internal fun Set<File>.collectDependencies(logger: Logger): List<ModDependencyInfo> {
    val ignoreTargets = mutableSetOf<String>()
    return mapNotNull {
        fun getModDependencyInfo(it: File): ModDependencyInfo? =
            JarFile(it).use { jar ->
                val modJsonEntry = jar.getJarEntry("fabric.mod.json")
                if (modJsonEntry == null) {
                    logger.info("No fabric.mod.json found in {}", it.path)
                    return null
                }
                val rawJson = jar.getInputStream(modJsonEntry).reader().readText()
                val modJson = json.decodeFromString<MiniFabricModJson>(rawJson)
                if (modJson.jars != null) {
                    val includedJars = modJson.jars.mapNotNull { jarEntry ->
                        val childPath = jarEntry.file
                        jar.getJarEntry(childPath)?.let { childEntry ->
                            val childFile = jar.getInputStream(childEntry).use { zip ->
                                val tempFile = createTempFile().toFile()
                                tempFile.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                                tempFile
                            }
                            getModDependencyInfo(childFile)?.also {
                                childFile.delete()
                            }
                        }
                    }
                    ignoreTargets += includedJars.map(ModDependencyInfo::id)
                }
                // ignore fabric-api modules(especially gametest-api)
                if (modJson.id != "fabric-api" && it.path.contains("net.fabricmc.fabric-api")) return null

                ModDependencyInfo(modJson.id, modJson.version)
            }

        getModDependencyInfo(it)
    }.filterNot { ignoreTargets.contains(it.id) }
}
