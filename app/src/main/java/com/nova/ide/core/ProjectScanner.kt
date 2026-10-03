package com.nova.ide.core

import java.io.File

data class ProjectInfo(
    val name: String, val hasGradle: Boolean, val hasKotlin: Boolean,
    val hasJava: Boolean, val hasManifest: Boolean
)

class ProjectScanner {
    fun scan(root: File): ProjectInfo {
        require(root.isDirectory) { "Project directory not found" }
        val all = root.walkTopDown().filter { it.isFile }.toList()
        return ProjectInfo(
            root.name,
            all.any { it.name == "build.gradle" || it.name == "build.gradle.kts" },
            all.any { it.extension == "kt" },
            all.any { it.extension == "java" },
            all.any { it.name == "AndroidManifest.xml" }
        )
    }
}
