/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp.context

import java.io.File

/**
 * Codebase context data structures
 */

/**
 * Represents a code file with its metadata and content
 */
data class CodeFile(
    val path: String,
    val relativePath: String,
    val language: FileLanguage,
    val content: String,
    val size: Long,
    val lastModified: Long
)

/**
 * Supported file languages
 */
enum class FileLanguage {
    KOTLIN,
    JAVA,
    XML,
    GRADLE_KTS,
    GRADLE,
    JSON,
    MARKDOWN,
    PROPERTIES,
    UNKNOWN
}

/**
 * Represents a Kotlin class/interface/object
 */
data class KotlinClass(
    val name: String,
    val type: ClassType,
    val packageName: String,
    val visibility: String,
    val modifiers: List<String>,
    val superTypes: List<String>,
    val functions: List<KotlinFunction>,
    val properties: List<KotlinProperty>,
    val documentation: String?
)

/**
 * Type of Kotlin class
 */
enum class ClassType {
    CLASS,
    INTERFACE,
    DATA_CLASS,
    OBJECT,
    COMPANION_OBJECT,
    ENUM,
    SEALED_CLASS
}

/**
 * Represents a Kotlin function
 */
data class KotlinFunction(
    val name: String,
    val visibility: String,
    val modifiers: List<String>,
    val parameters: List<KotlinParameter>,
    val returnType: String?,
    val isExtension: Boolean,
    val documentation: String?
)

/**
 * Represents a function parameter
 */
data class KotlinParameter(
    val name: String,
    val type: String,
    val defaultValue: String?
)

/**
 * Represents a Kotlin property
 */
data class KotlinProperty(
    val name: String,
    val type: String,
    val visibility: String,
    val modifiers: List<String>,
    val hasGetter: Boolean,
    val hasSetter: Boolean,
    val documentation: String?
)

/**
 * Represents a project structure
 */
data class ProjectStructure(
    val rootPath: String,
    val modules: List<ProjectModule>,
    val totalFiles: Int,
    val totalLines: Int
)

/**
 * Represents a project module
 */
data class ProjectModule(
    val name: String,
    val path: String,
    val type: ModuleType,
    val sourceFiles: List<CodeFile>,
    val dependencies: List<String>
)

/**
 * Type of module
 */
enum class ModuleType {
    APP,
    LIBRARY,
    FEATURE,
    COMMON,
    UNKNOWN
}

/**
 * Context information for AI prompts
 */
data class CodebaseContext(
    val currentFile: CodeFile?,
    val relatedFiles: List<CodeFile>,
    val projectStructure: ProjectStructure,
    val relevantClasses: List<KotlinClass>,
    val imports: List<String>,
    val summary: String
)
