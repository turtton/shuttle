# 🧵 shuttle

shuttle is a gradle plugin for generating mod dependencies to write `fabric.mod.json` from gradle dependency settings.

## Features

- [x] Auto generate `fabric.mod.json` from gradle dependencies
- [ ] Specify `suggests` or `recommends` or `ignore` dependencies

## Installation
<details open>
<summary>Kotlin DSL</summary>

```kotlin
plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("net.turtton.shuttle") version "VERSION" // Add
}
```

</details>

<details>
<summary>Groovy DSL</summary>

```groovy
plugins {
    id 'fabric-loom' version '1.7-SNAPSHOT'
    id 'net.turtton.shuttle' version 'VERSION'// Add
}
```
</details>

## Example

`build.gradle.kts`
```kotlin
dependencies {
    ...
    modImplementation("net.fabricmc:fabric-language-kotlin:1.11.0+kotlin.2.0.0")
    modApi("me.shedaniel.cloth:cloth-config-fabric:15.0.127") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modImplementation(include("dev.isxander:yet-another-config-lib:3.5.0+1.21-fabric"))
    modCompileOnly("com.terraformersmc:modmenu:11.0.1")
}
```

`build/libs/XXX-1.0.0.jar/fabric.mod.json`
```json
{
  ...
  "depends": {
    ...
    "fabric-language-kotlin": "1.11.0+kotlin.2.0.0",
    "cloth-config": "15.0.127"
  }
, // This is not bug
  "recommends": {
    "modmenu": "11.0.1"
  }
}
```