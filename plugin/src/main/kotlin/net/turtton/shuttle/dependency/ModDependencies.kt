package net.turtton.shuttle.dependency

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.FileCopyDetails
import org.slf4j.Logger

class ModDependencies(configurations: ConfigurationContainer, logger: Logger) {
    private val modDependencies: List<ModDependencyInfo>
    private val optionalDependencies: List<ModDependencyInfo>

    init {
        // https://github.com/FabricMC/fabric-loom/blob/fa8bf5ede3c8fa3e9d4c524efb974f80e4159367/src/main/java/net/fabricmc/loom/configuration/LoomConfigurations.java#L97
        val include = configurations.getByName("includeInternal")
        val includeDependencies = include.resolve().collectDependencies(logger)
        includeDependencies.forEach { logger.info("Ignore dependency: {}", it) }
        val ignoreTargets = includeDependencies.map(ModDependencyInfo::id).toMutableSet()

        val modCompileOnly = configurations.getByName("modCompileOnly")
        optionalDependencies = modCompileOnly.resolve().collectDependencies(logger).filterNot { ignoreTargets.contains(it.id) }
        optionalDependencies.forEach { logger.info("Optional dependency: {}", it) }
        ignoreTargets += optionalDependencies.map(ModDependencyInfo::id)

        val modImplementation = configurations.getByName("modImplementation")
        val implementationDependencies = modImplementation.resolve().collectDependencies(logger).filterNot { ignoreTargets.contains(it.id) }
        val modApi = configurations.getByName("modApi")
        val apiDependencies = modApi.resolve().collectDependencies(logger).filterNot { ignoreTargets.contains(it.id) }
        modDependencies = implementationDependencies + apiDependencies
        modDependencies.forEach { logger.info("Mod dependency: {}", it) }
    }

    /**
     * Write dependencies to fabric.mod.json
     *
     * To keep compatibility with other processes, this function edits raw text.
     */
    fun writeDependencies(details: FileCopyDetails) {
        val data = Json.decodeFromString<JsonObject>(details.file.readText())
        val depends = data[DependencyType.DEPENDS.key]?.jsonObject
        val recommends = data[DependencyType.RECOMMENDS.key]?.jsonObject
        // val suggests = data[DependencyType.SUGGESTS.key]?.jsonObject

        val dependencyTargets = DependencyType.entries.toMutableSet()
        var mode: DependencyType? = null
        details.filter { line ->
            if (line.contains("}")) mode = null
            when (mode) {
                DependencyType.DEPENDS -> {
                    mode = null
                    dependencyTargets.remove(DependencyType.DEPENDS)
                    val spaces = line.takeWhile { it == ' ' || it == '\t' }
                    val elements = generateElementString(modDependencies, depends, spaces)
                    return@filter "$line$elements"
                }
                DependencyType.RECOMMENDS -> {
                    mode = null
                    dependencyTargets.remove(DependencyType.RECOMMENDS)
                    val spaces = line.takeWhile { it == ' ' || it == '\t' }
                    val element = generateElementString(optionalDependencies, recommends, spaces)
                    return@filter "$line$element"
                }
                DependencyType.SUGGESTS -> {
                    mode = null
                    dependencyTargets.remove(DependencyType.SUGGESTS)
                    return@filter line
                    // val spaces = line.takeWhile { line == ' ' || line == '\t' }
                    // val elements = generateElementString(optionalDependencies, suggests, spaces)
                    // return@filter "$line$elements"
                }
                null -> {
                    for (entry in DependencyType.entries) {
                        if (line.contains(entry.key)) {
                            if (mode != null) {
                                error("Unexpected json entry. Hint: Make sure that the contents of fabric.mod.json are properly newlined")
                            }
                            mode = entry
                        }
                    }
                    if (line == "}") {
                        val additionalDependencies = dependencyTargets.joinToString("") {
                            val obj = "\t\"${it.key}\": {"
                            val dependencies = when (it) {
                                DependencyType.DEPENDS -> modDependencies
                                DependencyType.RECOMMENDS -> optionalDependencies
                                DependencyType.SUGGESTS -> emptyList()
                            }
                            val elements = generateElementString(dependencies, null, "\t\t")
                            if (elements.isNotBlank()) {
                                ",\n$obj$elements\n\t}"
                            } else {
                                ""
                            }
                        }
                        if (additionalDependencies.isNotBlank()) {
                            return@filter "$additionalDependencies\n$line"
                        }
                    }
                    line
                }
            }
        }
    }

    private fun generateElementString(dependencies: List<ModDependencyInfo>, targetObject: JsonObject? = null, indent: String? = null): String {
        val elements = dependencies.withIndex().mapNotNull { (index, it) ->
            // Ignore existing dependencies
            if (targetObject?.contains(it.id) != true) {
                val element = "\"${it.id}\": \"${it.version}\""
                if (index == dependencies.size - 1 && targetObject?.isEmpty() != false) {
                    element
                } else {
                    "$element,"
                }
            } else {
                null
            }
        }
        val lineBreaks = if (indent != null) "\n$indent" else "\n"
        return if (elements.isNotEmpty()) elements.joinToString(lineBreaks, prefix = lineBreaks) else ""
    }

    private enum class DependencyType(val key: String) {
        DEPENDS("depends"),
        RECOMMENDS("recommends"),
        SUGGESTS("suggests"),
    }
}
