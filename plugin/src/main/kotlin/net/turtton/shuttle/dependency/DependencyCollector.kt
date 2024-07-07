package net.turtton.shuttle.dependency

import java.io.File
import java.util.jar.JarFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
}

// https://json.schemastore.org/fabric.mod.json
@Serializable
private data class MiniFabricModJson(val id: String, val version: String)

internal fun Set<File>.collectDependencies(): List<ModDependencyInfo> {
    return mapNotNull {
        val jar = JarFile(it)
        val modJsonEntry = jar.getJarEntry("fabric.mod.json")
        val rawJson = jar.getInputStream(modJsonEntry).reader().readText()
        val modJson = json.decodeFromString<MiniFabricModJson>(rawJson)
        // ignore fabric-api modules
        if (modJson.id != "fabric-api" && it.path.contains("net.fabricmc.fabric-api")) return@mapNotNull null

        ModDependencyInfo(modJson.id, modJson.version)
    }
}
