package net.turtton.shuttle.task

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.turtton.shuttle.dependency.ModDependencies
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.language.jvm.tasks.ProcessResources

object ProcessFabricModJson : Action<Task> {
    override fun execute(task: Task) {
        val processResources = task as? ProcessResources
            ?: error("processResources task type should be ProcessResources, but ${task::class.simpleName} found")
        processResources.doFirst {
            val configurations = it.project.configurations
            val modDependencies = ModDependencies(configurations)
        }
        processResources.filesMatching("fabric.mod.json") { details ->
            val data = Json.decodeFromString<JsonObject>(details.file.readText())
            val depends = data["depends"]?.jsonObject
            var mode: String? = null
            details.filter {
                if (mode == "depends") {
                    println(it)
                    mode = null
                    val spaces = it.takeWhile { it == ' ' || it == '\t' }
                    return@filter "$it\n$spaces\"test\": \"99999\","
                }
                if (it.contains("}")) mode = null
                if (it.contains("depends")) mode = "depends"

                it
            }
        }
        processResources.doLast {
            processResources.destinationDir.resolve("fabric.mod.json").readText().also(::println)
        }
    }
}
