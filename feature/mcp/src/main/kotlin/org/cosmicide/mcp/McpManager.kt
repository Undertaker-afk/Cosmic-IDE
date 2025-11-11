/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cosmicide.mcp.context.CodebaseContext
import org.cosmicide.mcp.context.CodebaseContextEngine
import org.cosmicide.mcp.servers.DeepWikiMcpServer
import org.cosmicide.mcp.servers.ExaMcpServer
import org.cosmicide.mcp.servers.GrepMcpServer
import java.io.File

/**
 * Manager for all MCP servers and codebase context
 */
class McpManager(projectRootPath: String) {
    
    private val exaServer = ExaMcpServer()
    private val grepServer = GrepMcpServer()
    private val deepWikiServer = DeepWikiMcpServer()
    
    private val contextEngine = CodebaseContextEngine(File(projectRootPath))
    
    private var initialized = false
    
    /**
     * Initialize all MCP servers
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            // Initialize servers in parallel (best effort)
            runCatching { exaServer.initialize() }
            runCatching { grepServer.initialize() }
            runCatching { deepWikiServer.initialize() }
            
            initialized = true
        } catch (e: Exception) {
            // Partial initialization is okay
            initialized = true
        }
    }
    
    /**
     * Get codebase context
     */
    fun getCodebaseContext(currentFilePath: String? = null): CodebaseContext {
        return contextEngine.buildContext(currentFilePath)
    }
    
    /**
     * Read a file
     */
    fun readFile(filePath: String): String? {
        return contextEngine.getFileContent(filePath)
    }
    
    /**
     * Write to a file
     */
    fun writeFile(filePath: String, content: String): Boolean {
        return contextEngine.writeFileContent(filePath, content)
    }
    
    /**
     * List files in directory
     */
    fun listFiles(dirPath: String): List<String> {
        return contextEngine.listFiles(dirPath)
    }
    
    /**
     * Search the web with Exa
     */
    suspend fun searchWeb(query: String, numResults: Int = 10): ToolCallResult {
        return if (exaServer.isInitialized()) {
            exaServer.search(query, numResults)
        } else {
            ToolCallResult(
                content = listOf(ContentItem(type = "text", text = "Exa server not initialized")),
                isError = true
            )
        }
    }
    
    /**
     * Find similar content with Exa
     */
    suspend fun findSimilar(url: String, numResults: Int = 10): ToolCallResult {
        return if (exaServer.isInitialized()) {
            exaServer.findSimilar(url, numResults)
        } else {
            ToolCallResult(
                content = listOf(ContentItem(type = "text", text = "Exa server not initialized")),
                isError = true
            )
        }
    }
    
    /**
     * Search code on GitHub with Grep.app
     */
    suspend fun searchCode(
        query: String,
        language: String? = null,
        repo: String? = null
    ): ToolCallResult {
        return if (grepServer.isInitialized()) {
            grepServer.searchCode(query, language, repo)
        } else {
            ToolCallResult(
                content = listOf(ContentItem(type = "text", text = "Grep server not initialized")),
                isError = true
            )
        }
    }
    
    /**
     * Search knowledge base with DeepWiki
     */
    suspend fun searchKnowledge(query: String, maxResults: Int = 10): ToolCallResult {
        return if (deepWikiServer.isInitialized()) {
            deepWikiServer.search(query, maxResults)
        } else {
            ToolCallResult(
                content = listOf(ContentItem(type = "text", text = "DeepWiki server not initialized")),
                isError = true
            )
        }
    }
    
    /**
     * Search documentation with DeepWiki
     */
    suspend fun searchDocs(
        query: String,
        language: String? = null,
        framework: String? = null
    ): ToolCallResult {
        return if (deepWikiServer.isInitialized()) {
            deepWikiServer.searchDocs(query, language, framework)
        } else {
            ToolCallResult(
                content = listOf(ContentItem(type = "text", text = "DeepWiki server not initialized")),
                isError = true
            )
        }
    }
    
    /**
     * Build AI prompt with full context
     */
    fun buildAIPrompt(
        userMessage: String,
        currentFilePath: String? = null,
        includeCodebaseContext: Boolean = true
    ): String {
        val builder = StringBuilder()
        
        if (includeCodebaseContext) {
            val context = getCodebaseContext(currentFilePath)
            
            builder.appendLine("=== CODEBASE CONTEXT ===")
            builder.appendLine(context.summary)
            builder.appendLine()
            
            if (!context.imports.isEmpty()) {
                builder.appendLine("Imports:")
                context.imports.take(10).forEach { import ->
                    builder.appendLine("  - $import")
                }
                builder.appendLine()
            }
            
            if (context.relevantClasses.isNotEmpty()) {
                builder.appendLine("Relevant Classes:")
                context.relevantClasses.take(3).forEach { cls ->
                    builder.appendLine("  - ${cls.packageName}.${cls.name} (${cls.type})")
                }
                builder.appendLine()
            }
            
            context.currentFile?.let { file ->
                builder.appendLine("Current File Content:")
                builder.appendLine("```${file.language.name.lowercase()}")
                builder.appendLine(file.content.take(2000)) // Limit to 2000 chars
                if (file.content.length > 2000) {
                    builder.appendLine("... (truncated)")
                }
                builder.appendLine("```")
                builder.appendLine()
            }
        }
        
        builder.appendLine("=== USER MESSAGE ===")
        builder.appendLine(userMessage)
        
        return builder.toString()
    }
    
    /**
     * Get available tools description for AI
     */
    fun getAvailableToolsDescription(): String {
        return """
        Available Tools:
        
        File Operations:
        - read_file(path): Read content of a file
        - write_file(path, content): Write content to a file
        - list_files(path): List files in a directory
        
        Web Search (Exa):
        - search_web(query, num_results): Search the web
        - find_similar(url, num_results): Find similar content to a URL
        
        Code Search (Grep.app):
        - search_code(query, language, repo): Search code on GitHub
        
        Knowledge Base (DeepWiki):
        - search_knowledge(query, max_results): Search documentation
        - search_docs(query, language, framework): Search programming docs
        
        Context:
        - get_codebase_context(file_path): Get project structure and context
        """.trimIndent()
    }
    
    /**
     * Check if manager is initialized
     */
    fun isInitialized(): Boolean = initialized
}
