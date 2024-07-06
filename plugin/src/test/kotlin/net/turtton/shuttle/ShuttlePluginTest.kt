package net.turtton.shuttle

import kotlin.test.Test
import kotlin.test.assertNotNull
import org.gradle.testfixtures.ProjectBuilder

/**
 * A simple unit test for the plugin.
 */
class ShuttlePluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("net.turtton.shuttle")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
    }
}
