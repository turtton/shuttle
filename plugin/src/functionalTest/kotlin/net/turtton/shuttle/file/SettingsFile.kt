package net.turtton.shuttle.file

import java.io.File
import kotlin.reflect.KProperty0

class SettingsFile(projectFile: KProperty0<File>) : AbstractTestFile(projectFile, "settings.gradle") {
    override fun create() {
        file.writeText(
            // language=groovy
            """
            pluginManagement {
            	repositories {
            		maven {
            			name = 'Fabric'
            			url = 'https://maven.fabricmc.net/'
            		}
            		mavenCentral()
            		gradlePluginPortal()
            	}
            }
            """.trimIndent(),
        )
    }
}
