package net.turtton.shuttle.dependency

import org.gradle.api.artifacts.ConfigurationContainer

class ModDependencies(configurations: ConfigurationContainer) {
    private val modDependencies: List<ModDependencyInfo>
    private val optionalDependencies: List<ModDependencyInfo>

    init {
        // https://github.com/FabricMC/fabric-loom/blob/fa8bf5ede3c8fa3e9d4c524efb974f80e4159367/src/main/java/net/fabricmc/loom/configuration/LoomConfigurations.java#L97
        val include = configurations.getByName("includeInternal")
        val ignoreTargets = include.resolve().collectDependencies().map(ModDependencyInfo::id).toMutableSet()

        val modCompileOnly = configurations.getByName("modCompileOnly")
        optionalDependencies = modCompileOnly.resolve().collectDependencies().filterNot { ignoreTargets.contains(it.id) }
        ignoreTargets += optionalDependencies.map(ModDependencyInfo::id)

        val modImplementation = configurations.getByName("modImplementation")
        modDependencies = modImplementation.resolve().collectDependencies().filterNot { ignoreTargets.contains(it.id) }
    }
}
