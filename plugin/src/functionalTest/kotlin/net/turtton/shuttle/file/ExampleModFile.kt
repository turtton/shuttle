package net.turtton.shuttle.file

import java.io.File
import kotlin.reflect.KProperty0

class ExampleModFile(projectFile: KProperty0<File>) : AbstractTestFile(projectFile, "src/main/java/com/example/ExampleMod.java") {
    override fun create() {
        file.parentFile.mkdirs()
        file.writeText(
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
    }
}
