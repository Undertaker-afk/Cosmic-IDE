/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp.context

import java.io.File

/**
 * Analyzes Kotlin codebase and extracts structure information
 */
class KotlinCodeAnalyzer {

    /**
     * Analyze a Kotlin source file
     */
    fun analyzeFile(file: File): List<KotlinClass> {
        if (!file.exists() || !file.canRead()) {
            return emptyList()
        }

        val content = file.readText()
        return parseKotlinClasses(content, file.name)
    }

    /**
     * Parse Kotlin classes from source code
     * This is a simplified parser that uses regex patterns
     */
    private fun parseKotlinClasses(content: String, fileName: String): List<KotlinClass> {
        val classes = mutableListOf<KotlinClass>()
        
        // Extract package name
        val packageName = extractPackageName(content)
        
        // Match class declarations
        val classPattern = Regex(
            """((?:public|private|internal|protected)?\s*(?:abstract|open|final|sealed)?\s*(?:data|enum|object)?\s*(?:class|interface|object))\s+(\w+)"""
        )
        
        classPattern.findAll(content).forEach { match ->
            val declaration = match.groupValues[1].trim()
            val className = match.groupValues[2]
            
            val classType = when {
                "data class" in declaration -> ClassType.DATA_CLASS
                "sealed class" in declaration -> ClassType.SEALED_CLASS
                "enum class" in declaration -> ClassType.ENUM
                "object" in declaration -> ClassType.OBJECT
                "interface" in declaration -> ClassType.INTERFACE
                else -> ClassType.CLASS
            }
            
            val visibility = extractVisibility(declaration)
            val modifiers = extractModifiers(declaration)
            
            // Extract functions and properties in this class
            val functions = extractFunctions(content, className)
            val properties = extractProperties(content, className)
            
            classes.add(
                KotlinClass(
                    name = className,
                    type = classType,
                    packageName = packageName,
                    visibility = visibility,
                    modifiers = modifiers,
                    superTypes = emptyList(), // Simplified
                    functions = functions,
                    properties = properties,
                    documentation = null
                )
            )
        }
        
        return classes
    }

    /**
     * Extract package name from source
     */
    private fun extractPackageName(content: String): String {
        val packagePattern = Regex("""package\s+([\w.]+)""")
        return packagePattern.find(content)?.groupValues?.get(1) ?: ""
    }

    /**
     * Extract visibility modifier
     */
    private fun extractVisibility(declaration: String): String {
        return when {
            "public" in declaration -> "public"
            "private" in declaration -> "private"
            "internal" in declaration -> "internal"
            "protected" in declaration -> "protected"
            else -> "public" // Default in Kotlin
        }
    }

    /**
     * Extract other modifiers
     */
    private fun extractModifiers(declaration: String): List<String> {
        val modifiers = mutableListOf<String>()
        if ("abstract" in declaration) modifiers.add("abstract")
        if ("open" in declaration) modifiers.add("open")
        if ("final" in declaration) modifiers.add("final")
        if ("sealed" in declaration) modifiers.add("sealed")
        if ("data" in declaration) modifiers.add("data")
        return modifiers
    }

    /**
     * Extract functions from class (simplified)
     */
    private fun extractFunctions(content: String, className: String): List<KotlinFunction> {
        val functions = mutableListOf<KotlinFunction>()
        
        val functionPattern = Regex(
            """((?:public|private|internal|protected)?\s*(?:suspend|inline|infix|operator)?\s*fun)\s+(\w+)\s*\((.*?)\)(?:\s*:\s*([\w.<>?]+))?"""
        )
        
        functionPattern.findAll(content).forEach { match ->
            val declaration = match.groupValues[1]
            val functionName = match.groupValues[2]
            val params = match.groupValues[3]
            val returnType = match.groupValues.getOrNull(4)
            
            val visibility = extractVisibility(declaration)
            val modifiers = extractFunctionModifiers(declaration)
            val parameters = parseParameters(params)
            
            functions.add(
                KotlinFunction(
                    name = functionName,
                    visibility = visibility,
                    modifiers = modifiers,
                    parameters = parameters,
                    returnType = returnType,
                    isExtension = false,
                    documentation = null
                )
            )
        }
        
        return functions
    }

    /**
     * Extract properties from class (simplified)
     */
    private fun extractProperties(content: String, className: String): List<KotlinProperty> {
        val properties = mutableListOf<KotlinProperty>()
        
        val propertyPattern = Regex(
            """((?:public|private|internal|protected)?\s*(?:val|var))\s+(\w+)\s*:\s*([\w.<>?]+)"""
        )
        
        propertyPattern.findAll(content).forEach { match ->
            val declaration = match.groupValues[1]
            val propertyName = match.groupValues[2]
            val type = match.groupValues[3]
            
            val visibility = extractVisibility(declaration)
            val isMutable = "var" in declaration
            
            properties.add(
                KotlinProperty(
                    name = propertyName,
                    type = type,
                    visibility = visibility,
                    modifiers = if (isMutable) listOf("mutable") else emptyList(),
                    hasGetter = true,
                    hasSetter = isMutable,
                    documentation = null
                )
            )
        }
        
        return properties
    }

    /**
     * Extract function modifiers
     */
    private fun extractFunctionModifiers(declaration: String): List<String> {
        val modifiers = mutableListOf<String>()
        if ("suspend" in declaration) modifiers.add("suspend")
        if ("inline" in declaration) modifiers.add("inline")
        if ("infix" in declaration) modifiers.add("infix")
        if ("operator" in declaration) modifiers.add("operator")
        return modifiers
    }

    /**
     * Parse function parameters
     */
    private fun parseParameters(paramsStr: String): List<KotlinParameter> {
        if (paramsStr.isBlank()) return emptyList()
        
        return paramsStr.split(",").mapNotNull { param ->
            val trimmed = param.trim()
            if (trimmed.isEmpty()) return@mapNotNull null
            
            val parts = trimmed.split(":")
            if (parts.size < 2) return@mapNotNull null
            
            val name = parts[0].trim()
            val typeAndDefault = parts[1].trim().split("=")
            val type = typeAndDefault[0].trim()
            val default = typeAndDefault.getOrNull(1)?.trim()
            
            KotlinParameter(name, type, default)
        }
    }

    /**
     * Extract imports from source
     */
    fun extractImports(content: String): List<String> {
        val importPattern = Regex("""import\s+([\w.]+)""")
        return importPattern.findAll(content)
            .map { it.groupValues[1] }
            .toList()
    }
}
