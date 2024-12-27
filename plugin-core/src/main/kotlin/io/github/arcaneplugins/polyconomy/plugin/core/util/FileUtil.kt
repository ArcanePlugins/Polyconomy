package io.github.arcaneplugins.polyconomy.plugin.core.util

import java.io.File
import java.io.InputStream

object FileUtil {

    fun saveResource(fileName: String, outputDir: String) {
        val resource: InputStream? = object {}.javaClass.getResourceAsStream("/$fileName")
        if (resource == null) {
            throw IllegalArgumentException("Resource file '$fileName' not found in JAR.")
        }

        val outputFile = File(outputDir, fileName)
        outputFile.parentFile?.mkdirs()

        resource.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

}