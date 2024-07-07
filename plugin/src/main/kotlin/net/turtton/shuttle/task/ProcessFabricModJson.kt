package net.turtton.shuttle.task

import net.turtton.shuttle.dependency.ModDependencies
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.language.jvm.tasks.ProcessResources

object ProcessFabricModJson : Action<Task> {
    override fun execute(task: Task) {
        val processResources = task as? ProcessResources
            ?: error("processResources task type should be ProcessResources, but ${task::class.simpleName} found")
        var modDependencies: ModDependencies? = null
        processResources.doFirst {
            val configurations = it.project.configurations
            modDependencies = ModDependencies(configurations, it.logger)
        }
        processResources.filesMatching("fabric.mod.json") { modDependencies?.writeDependencies(it) }
    }
}
