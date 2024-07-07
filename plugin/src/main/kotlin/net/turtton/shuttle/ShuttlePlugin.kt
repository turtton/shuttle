package net.turtton.shuttle

import net.turtton.shuttle.task.ProcessFabricModJson
import org.gradle.api.Plugin
import org.gradle.api.Project

class ShuttlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.getByName("processResources", ProcessFabricModJson)
    }
}
