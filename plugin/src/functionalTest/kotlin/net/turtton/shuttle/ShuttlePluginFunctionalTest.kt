package net.turtton.shuttle

import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.turtton.shuttle.file.AbstractTestFile
import net.turtton.shuttle.file.BuildFile
import net.turtton.shuttle.file.ExampleModFile
import net.turtton.shuttle.file.ModJsonFile
import net.turtton.shuttle.file.PropertyFile
import net.turtton.shuttle.file.SettingsFile
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

/**
 * A simple functional test for the plugin.
 */
class ShuttlePluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val testFiles = setOf(
        SettingsFile(::projectDir),
        BuildFile(::projectDir),
        PropertyFile(::projectDir),
        ExampleModFile(::projectDir),
        ModJsonFile(::projectDir),
    )

    @Test fun `can run task`() {
        // Set up the test build
        // Original: https://github.com/FabricMC/fabric-example-mod
        testFiles.forEach(AbstractTestFile::create)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("processResources", "--stacktrace", "--info")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        // Verify the result
        val fabricModJson = projectDir.resolve("build/resources/main/fabric.mod.json")
        assertTrue(fabricModJson.exists())
        val json = Json.decodeFromString<JsonObject>(fabricModJson.readText())
        val depends = json["depends"]?.jsonObject
        assertNotNull(depends)
        assertTrue(depends.containsKey("fabric-language-kotlin"))
        assertTrue(depends.containsKey("cloth-config"))
        assertTrue(!depends.containsKey("modmenu"))

        val recommends = json["recommends"]?.jsonObject
        assertNotNull(recommends)
        assertTrue(!recommends.containsKey("yet-another-config-lib"))
        assertTrue(recommends.containsKey("modmenu"))
    }
}
