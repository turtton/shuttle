package net.turtton.shuttle

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.jvm.tasks.ProcessResources

class ShuttlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.getByName("processResources") { task ->
            val processResources = task as? ProcessResources ?: error("processResources task type should be ProcessResources, but ${task::class.simpleName} found")
            processResources.doFirst {
                val configurations = project.configurations
                val modImplementation = configurations.getByName("modImplementation")
                val modDependencies = modImplementation.resolve().collectDependencies()
                println(modDependencies)
                // https://github.com/FabricMC/fabric-loom/blob/fa8bf5ede3c8fa3e9d4c524efb974f80e4159367/src/main/java/net/fabricmc/loom/configuration/LoomConfigurations.java#L97
                val include = configurations.getByName("includeInternal")
                val includeDependencies = include.resolve().collectDependencies()
                println(includeDependencies)
            }
            processResources.filesMatching("fabric.mod.json") { details ->
                val data = Json.decodeFromString<JsonObject>(details.file.readText())
                val depends = data["depends"]?.jsonObject
                println(depends?.entries)
            }
        }
    }
}
