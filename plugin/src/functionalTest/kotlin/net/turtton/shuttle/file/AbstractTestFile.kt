package net.turtton.shuttle.file

import java.io.File
import kotlin.reflect.KProperty0

abstract class AbstractTestFile(projectFile: KProperty0<File>, fileName: String) {
    private val projectFile by projectFile
    val file: File by lazy { this.projectFile.resolve(fileName) }

    abstract fun create()
}
