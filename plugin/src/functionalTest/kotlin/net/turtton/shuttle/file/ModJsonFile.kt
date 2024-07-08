package net.turtton.shuttle.file

import java.io.File
import kotlin.reflect.KProperty0

class ModJsonFile(projectFile: KProperty0<File>) : AbstractTestFile(projectFile, "src/main/resources/fabric.mod.json") {
    override fun create() {
        file.parentFile.mkdirs()
        file.writeText(
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
    }
}
