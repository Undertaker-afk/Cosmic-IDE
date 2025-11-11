/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp.context

import java.io.File

/**
 * Main engine for building codebase context for AI prompts
 */
class CodebaseContextEngine(private val projectRoot: File) {

    private val analyzer = KotlinCodeAnalyzer()

    /**
     * Build comprehensive codebase context
     */
    fun buildContext(currentFilePath: String? = null): CodebaseContext {
        val projectStructure = analyzeProjectStructure()
        val currentFile = currentFilePath?.let { loadCodeFile(File(it)) }
        val relatedFiles = findRelatedFiles(currentFile)
        val relevantClasses = extractRelevantClasses(currentFile, relatedFiles)
        val imports = currentFile?.let { analyzer.extractImports(it.content) } ?: emptyList()
        val summary = generateSummary(projectStructure, currentFile)

        return CodebaseContext(
            currentFile = currentFile,
            relatedFiles = relatedFiles,
            projectStructure = projectStructure,
            relevantClasses = relevantClasses,
            imports = imports,
            summary = summary
        )
    }

    /**
     * Analyze the entire project structure
     */
    private fun analyzeProjectStructure(): ProjectStructure {
        val modules = mutableListOf<ProjectModule>()
        var totalFiles = 0
        var totalLines = 0

        projectRoot.walkTopDown()
            .filter { it.isDirectory && File(it, "build.gradle.kts").exists() }
            .forEach { moduleDir ->
                val module = analyzeModule(moduleDir)
                modules.add(module)
                totalFiles += module.sourceFiles.size
                totalLines += module.sourceFiles.sumOf { 
                    it.content.lines().size 
                }
            }

        return ProjectStructure(
            rootPath = projectRoot.absolutePath,
            modules = modules,
            totalFiles = totalFiles,
            totalLines = totalLines
        )
    }

    /**
     * Analyze a single module
     */
    private fun analyzeModule(moduleDir: File): ProjectModule {
        val sourceFiles = mutableListOf<CodeFile>()
        val moduleName = moduleDir.name
        
        // Determine module type
        val moduleType = when {
            moduleName == "app" -> ModuleType.APP
            moduleName == "common" -> ModuleType.COMMON
            moduleName.startsWith("feature") -> ModuleType.FEATURE
            else -> ModuleType.LIBRARY
        }

        // Find source files
        val srcDir = File(moduleDir, "src/main")
        if (srcDir.exists()) {
            srcDir.walkTopDown()
                .filter { it.isFile && isSourceFile(it) }
                .forEach { file ->
                    loadCodeFile(file)?.let { sourceFiles.add(it) }
                }
        }

        // Extract dependencies from build.gradle.kts
        val dependencies = extractDependencies(moduleDir)

        return ProjectModule(
            name = moduleName,
            path = moduleDir.absolutePath,
            type = moduleType,
            sourceFiles = sourceFiles,
            dependencies = dependencies
        )
    }

    /**
     * Load a code file
     */
    private fun loadCodeFile(file: File): CodeFile? {
        if (!file.exists() || !file.canRead()) {
            return null
        }

        return CodeFile(
            path = file.absolutePath,
            relativePath = file.relativeTo(projectRoot).path,
            language = detectLanguage(file),
            content = file.readText(),
            size = file.length(),
            lastModified = file.lastModified()
        )
    }

    /**
     * Detect file language
     */
    private fun detectLanguage(file: File): FileLanguage {
        return when (file.extension.lowercase()) {
            "kt" -> FileLanguage.KOTLIN
            "java" -> FileLanguage.JAVA
            "xml" -> FileLanguage.XML
            "kts" -> FileLanguage.GRADLE_KTS
            "gradle" -> FileLanguage.GRADLE
            "json" -> FileLanguage.JSON
            "md" -> FileLanguage.MARKDOWN
            "properties" -> FileLanguage.PROPERTIES
            else -> FileLanguage.UNKNOWN
        }
    }

    /**
     * Check if file is a source file
     */
    private fun isSourceFile(file: File): Boolean {
        return when (file.extension.lowercase()) {
            "kt", "java", "xml", "kts" -> true
            else -> false
        }
    }

    /**
     * Find files related to the current file
     */
    private fun findRelatedFiles(currentFile: CodeFile?): List<CodeFile> {
        if (currentFile == null) return emptyList()

        val related = mutableListOf<CodeFile>()
        val currentDir = File(currentFile.path).parentFile

        // Find files in the same package/directory
        currentDir?.walkTopDown()
            ?.maxDepth(1)
            ?.filter { it.isFile && isSourceFile(it) && it.absolutePath != currentFile.path }
            ?.forEach { file ->
                loadCodeFile(file)?.let { related.add(it) }
            }

        return related.take(5) // Limit to 5 related files
    }

    /**
     * Extract relevant classes from files
     */
    private fun extractRelevantClasses(
        currentFile: CodeFile?,
        relatedFiles: List<CodeFile>
    ): List<KotlinClass> {
        val classes = mutableListOf<KotlinClass>()

        // Analyze current file
        currentFile?.let {
            if (it.language == FileLanguage.KOTLIN) {
                classes.addAll(analyzer.analyzeFile(File(it.path)))
            }
        }

        // Analyze related files
        relatedFiles.forEach { file ->
            if (file.language == FileLanguage.KOTLIN) {
                classes.addAll(analyzer.analyzeFile(File(file.path)))
            }
        }

        return classes
    }

    /**
     * Extract dependencies from build.gradle.kts
     */
    private fun extractDependencies(moduleDir: File): List<String> {
        val buildFile = File(moduleDir, "build.gradle.kts")
        if (!buildFile.exists()) return emptyList()

        val content = buildFile.readText()
        val depPattern = Regex("""implementation\s*\(\s*"([^"]+)"\s*\)""")
        
        return depPattern.findAll(content)
            .map { it.groupValues[1] }
            .toList()
    }

    /**
     * Generate a summary of the codebase context
     */
    private fun generateSummary(
        projectStructure: ProjectStructure,
        currentFile: CodeFile?
    ): String {
        val builder = StringBuilder()
        
        builder.appendLine("Project: ${projectStructure.rootPath}")
        builder.appendLine("Modules: ${projectStructure.modules.size}")
        builder.appendLine("Total Files: ${projectStructure.totalFiles}")
        builder.appendLine("Total Lines: ${projectStructure.totalLines}")
        
        currentFile?.let {
            builder.appendLine("\nCurrent File: ${it.relativePath}")
            builder.appendLine("Language: ${it.language}")
            builder.appendLine("Size: ${it.size} bytes")
        }
        
        return builder.toString()
    }

    /**
     * Get file content for AI context
     */
    fun getFileContent(filePath: String): String? {
        val file = File(filePath)
        return if (file.exists() && file.canRead()) {
            file.readText()
        } else {
            null
        }
    }

    /**
     * Write file content (for AI editing)
     */
    fun writeFileContent(filePath: String, content: String): Boolean {
        return try {
            val file = File(filePath)
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * List files in directory
     */
    fun listFiles(dirPath: String): List<String> {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }

        return dir.listFiles()?.map { it.name } ?: emptyList()
    }
}
