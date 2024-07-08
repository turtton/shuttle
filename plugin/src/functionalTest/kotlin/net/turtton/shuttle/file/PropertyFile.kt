package net.turtton.shuttle.file

import java.io.File
import kotlin.reflect.KProperty0

class PropertyFile(projectFile: KProperty0<File>) : AbstractTestFile(projectFile, "gradle.properties") {
    override fun create() {
        file.writeText(
            // language=properties
            """
            # Done to increase the memory available to gradle.
            org.gradle.jvmargs=-Xmx1G
            org.gradle.parallel=true
            # Fabric Properties
            # check these on https://fabricmc.net/develop
            minecraft_version=1.21
            yarn_mappings=1.21+build.2
            loader_version=0.15.11
            # Mod Properties
            mod_version=1.0.0
            maven_group=com.example
            archives_base_name=modid
            # Dependencies
            fabric_version=0.100.3+1.21
            """.trimIndent(),
        )
    }
}
