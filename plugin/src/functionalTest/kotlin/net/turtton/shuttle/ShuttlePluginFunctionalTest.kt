package net.turtton.shuttle

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

/**
 * A simple functional test for the plugin.
 */
class ShuttlePluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }
    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val propertiesFile by lazy { projectDir.resolve("gradle.properties") }
    private val exampleModFile by lazy { projectDir.resolve("src/main/java/com/example/ExampleMod.java") }
    private val modJsonFile by lazy { projectDir.resolve("src/main/resources/fabric.mod.json") }

    @Test fun `can run task`() {
        // Set up the test build
        // Original: https://github.com/FabricMC/fabric-example-mod
        settingsFile.writeText(
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
        buildFile.writeText(
            // language=groovy
            """
            plugins {
            	id 'fabric-loom' version '1.7-SNAPSHOT'
                id 'net.turtton.shuttle'
            }
            
            version = project.mod_version
            group = project.maven_group
            
            base {
            	archivesName = project.archives_base_name
            }
            
            repositories {
            	// Add repositories to retrieve artifacts from in here.
            	// You should only use this when depending on other mods because
            	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
            	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
            	// for more information about repositories.
            }
            
            loom {
            	splitEnvironmentSourceSets()
            
            	mods {
            		"modid" {
            			sourceSet sourceSets.main
            			// sourceSet sourceSets.client
            		}
            	}
            
            }
            
            dependencies {
            	// To change the versions see the gradle.properties file
            	minecraft "com.mojang:minecraft:$\\{project.minecraft_version}"
            	mappings "net.fabricmc:yarn:$\\{project.yarn_mappings}:v2"
            	modImplementation "net.fabricmc:fabric-loader:$\\{project.loader_version}"
            
            	// Fabric API. This is technically optional, but you probably want it anyway.
            	modImplementation "net.fabricmc.fabric-api:fabric-api:$\\{project.fabric_version}"
            	
            }
            
            processResources {
            	inputs.property "version", project.version
            
            	filesMatching("fabric.mod.json") {
            		expand "version": project.version
            	}
            }
            
            tasks.withType(JavaCompile).configureEach {
            	it.options.release = 21
            }
            
            java {
            	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
            	// if it is present.
            	// If you remove this line, sources will not be generated.
            	withSourcesJar()
            
            	sourceCompatibility = JavaVersion.VERSION_21
            	targetCompatibility = JavaVersion.VERSION_21
            }
            
            jar {
            	from("LICENSE") {
            		rename { "$\\{it}_$\\{project.base.archivesName.get()}"}
            	}
            }
            """.trimIndent().replace("\\\\", ""),
        )
        propertiesFile.writeText(
            //  language=properties
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

        exampleModFile.parentFile.mkdirs()
        exampleModFile.writeText(
            // language=java
            """
            package com.example;
            
            import net.fabricmc.api.ModInitializer;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            
            public class ExampleMod implements ModInitializer {
                public static final Logger LOGGER = LoggerFactory.getLogger("modid");
            	@Override
            	public void onInitialize() {
            		LOGGER.info("Hello Fabric world!");
            	}
            }
            """.trimIndent(),
        )
        modJsonFile.parentFile.mkdirs()
        modJsonFile.writeText(
            // language=json
            """
            {
            	"schemaVersion": 1,
            	"id": "modid",
            	"version": "$\\{version}",
            	"name": "Example mod",
            	"description": "This is an example description! Tell everyone what your mod is about!",
            	"authors": [
            	],
            	"license": "CC0-1.0",
            	"environment": "*",
            	"entrypoints": {
            		"main": [
            			"com.example.ExampleMod"
            		]
            	},
            	"depends": {
            		"fabricloader": ">=0.15.11",
            		"minecraft": "~1.21",
            		"java": ">=21",
            		"fabric-api": "*"
            	}
            }
            """.trimIndent().replace("\\\\", ""),
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("processResources")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
        // Verify the result
        assertTrue(projectDir.resolve("build/resources/main/fabric.mod.json").exists())
    }
}
