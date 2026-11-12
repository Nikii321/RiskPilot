package com.evedenta.riskpilot.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class PackageArchitectureTest {
    @Test fun `one type per file and layer dependencies remain explicit`() {
        val root=Path.of("src/main/kotlin")
        val allowed=mapOf(
            "domain" to setOf("domain"),
            "ml" to setOf("ml","domain"),
            "repository" to setOf("repository","domain","model","ml","exception"),
            "controller" to setOf("controller","service","model","domain","config","exception"),
            "algorithm" to setOf("algorithm","domain","model"),
            "model" to setOf("model","domain","ml","config"),
            "config" to setOf("config","domain","repository"),
            "scheduler" to setOf("scheduler","service","config"),
            "exception" to setOf("exception")
        )
        Files.walk(root).use { paths -> paths.filter { it.toString().endsWith(".kt") }.forEach { file ->
            val text=Files.readString(file)
            val pkg=Regex("(?m)^package ([\\w.]+)").find(text)!!.groupValues[1]
            val expected=root.resolve(pkg.replace('.','/')).resolve(file.fileName)
            assertEquals(expected.normalize(),file.normalize(),"Package must match directory: $file")
            val types=Regex("(?m)^(?:data class|enum class|class|object)\\s+(\\w+)").findAll(text).map { it.groupValues[1] }.toList()
            assertEquals(listOf(file.fileName.toString().removeSuffix(".kt")),types,"One matching top-level type: $file")
            val layer=pkg.removePrefix("com.evedenta.riskpilot").trimStart('.').substringBefore('.')
            Regex("(?m)^import com\\.evedenta\\.riskpilot\\.([a-z]+)\\.").findAll(text).forEach { dependency ->
                val target=dependency.groupValues[1]
                if(layer=="service") assertTrue(target!="controller","Service must not depend on HTTP: $file")
                else if(layer in allowed) assertTrue(target in allowed.getValue(layer),"Forbidden $layer → $target dependency: $file")
            }
        } }
    }
}
