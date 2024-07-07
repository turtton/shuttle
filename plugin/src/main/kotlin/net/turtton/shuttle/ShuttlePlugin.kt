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
            processResources.filesMatching("fabric.mod.json") { details ->
                val data = Json.decodeFromString<JsonObject>(details.file.readText())
                val depends = data["depends"]?.jsonObject
                println(depends?.entries)
            }
        }
    }
}
