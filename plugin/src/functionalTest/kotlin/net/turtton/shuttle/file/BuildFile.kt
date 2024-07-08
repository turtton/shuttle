package net.turtton.shuttle.file

import java.io.File
import kotlin.reflect.KProperty0

class BuildFile(projectFile: KProperty0<File>) : AbstractTestFile(projectFile, "build.gradle") {
    override fun create() {
        file.writeText(
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
                maven { url "https://maven.shedaniel.me/" }
                maven { url "https://maven.terraformersmc.com/releases/" }
                maven {
                    name 'Xander Maven'
                    url 'https://maven.isxander.dev/releases'
                } 
                maven {
                    name = "Terraformers"
                    url = "https://maven.terraformersmc.com/"
                }
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
            
                // ==== Testing dependencies ====
                // General dependencies
                // https://github.com/FabricMC/fabric-language-kotlin#usage
                modImplementation("net.fabricmc:fabric-language-kotlin:1.11.0+kotlin.2.0.0")
            
                // Api dependencies
                // https://github.com/shedaniel/cloth-config
                modApi("me.shedaniel.cloth:cloth-config-fabric:15.0.127") {
                    exclude(group: "net.fabricmc.fabric-api")
                }
                
                // inlude dependencies
                // https://github.com/isXander/YetAnotherConfigLib
                modImplementation(include "dev.isxander:yet-another-config-lib:3.5.0+1.21-fabric")
            
                // compileOnly dependencies
                // https://github.com/TerraformersMC/ModMenu
                modCompileOnly "com.terraformersmc:modmenu:11.0.1"
            }
            
            processResources {
            	inputs.property "version", project.version
            
            	filesMatching("fabric.mod.json") {
            		expand "version": project.version
            	}
            
                doLast {
                    file(destinationDir.getAbsolutePath() + "/fabric.mod.json").eachLine { println it }
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
    }
}
