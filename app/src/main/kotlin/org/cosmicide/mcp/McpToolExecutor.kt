/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cosmicide.mcp.ToolCallResult
import org.cosmicide.mcp.ContentItem

/**
 * Executes tool calls requested by AI
 */
object McpToolExecutor {
    
    private const val TAG = "McpToolExecutor"
    
    /**
     * Parse and execute tool calls from AI response
     * Expected format: <tool>tool_name(arg1="value1", arg2="value2")</tool>
     */
    suspend fun executeToolCalls(aiResponse: String, currentFilePath: String? = null): String {
        val manager = McpProvider.getManager() ?: return aiResponse
        
        val toolPattern = Regex("""<tool>([\w_]+)\((.*?)\)</tool>""")
        var modifiedResponse = aiResponse
        
        toolPattern.findAll(aiResponse).forEach { match ->
            val toolName = match.groupValues[1]
            val argsString = match.groupValues[2]
            val args = parseArguments(argsString)
            
            try {
                val result = executeToolCall(manager, toolName, args, currentFilePath)
                val resultText = formatToolResult(result)
                
                // Replace tool call with result
                modifiedResponse = modifiedResponse.replace(match.value, resultText)
            } catch (e: Exception) {
                Log.e(TAG, "Error executing tool $toolName", e)
                modifiedResponse = modifiedResponse.replace(
                    match.value,
                    "[Error executing $toolName: ${e.message}]"
                )
            }
        }
        
        return modifiedResponse
    }
    
    /**
     * Execute a single tool call
     */
    private suspend fun executeToolCall(
        manager: org.cosmicide.mcp.McpManager,
        toolName: String,
        args: Map<String, String>,
        currentFilePath: String?
    ): ToolCallResult = withContext(Dispatchers.IO) {
        when (toolName) {
            "read_file" -> {
                val path = args["path"] ?: throw IllegalArgumentException("path is required")
                val content = manager.readFile(path)
                ToolCallResult(
                    content = listOf(ContentItem(type = "text", text = content ?: "File not found")),
                    isError = content == null
                )
            }
            
            "write_file" -> {
                val path = args["path"] ?: throw IllegalArgumentException("path is required")
                val content = args["content"] ?: throw IllegalArgumentException("content is required")
                val success = manager.writeFile(path, content)
                ToolCallResult(
                    content = listOf(
                        ContentItem(
                            type = "text",
                            text = if (success) "File written successfully" else "Failed to write file"
                        )
                    ),
                    isError = !success
                )
            }
            
            "list_files" -> {
                val path = args["path"] ?: throw IllegalArgumentException("path is required")
                val files = manager.listFiles(path)
                ToolCallResult(
                    content = listOf(
                        ContentItem(
                            type = "text",
                            text = files.joinToString("\n")
                        )
                    ),
                    isError = false
                )
            }
            
            "search_web" -> {
                val query = args["query"] ?: throw IllegalArgumentException("query is required")
                val numResults = args["num_results"]?.toIntOrNull() ?: 10
                manager.searchWeb(query, numResults)
            }
            
            "find_similar" -> {
                val url = args["url"] ?: throw IllegalArgumentException("url is required")
                val numResults = args["num_results"]?.toIntOrNull() ?: 10
                manager.findSimilar(url, numResults)
            }
            
            "search_code" -> {
                val query = args["query"] ?: throw IllegalArgumentException("query is required")
                val language = args["language"]
                val repo = args["repo"]
                manager.searchCode(query, language, repo)
            }
            
            "search_knowledge" -> {
                val query = args["query"] ?: throw IllegalArgumentException("query is required")
                val maxResults = args["max_results"]?.toIntOrNull() ?: 10
                manager.searchKnowledge(query, maxResults)
            }
            
            "search_docs" -> {
                val query = args["query"] ?: throw IllegalArgumentException("query is required")
                val language = args["language"]
                val framework = args["framework"]
                manager.searchDocs(query, language, framework)
            }
            
            "get_codebase_context" -> {
                val filePath = args["file_path"] ?: currentFilePath
                val context = manager.getCodebaseContext(filePath)
                ToolCallResult(
                    content = listOf(
                        ContentItem(
                            type = "text",
                            text = context.summary
                        )
                    ),
                    isError = false
                )
            }
            
            else -> {
                ToolCallResult(
                    content = listOf(
                        ContentItem(
                            type = "text",
                            text = "Unknown tool: $toolName"
                        )
                    ),
                    isError = true
                )
            }
        }
    }
    
    /**
     * Parse arguments from string
     * Format: arg1="value1", arg2="value2"
     */
    private fun parseArguments(argsString: String): Map<String, String> {
        if (argsString.isBlank()) return emptyMap()
        
        val args = mutableMapOf<String, String>()
        val argPattern = Regex("""(\w+)="([^"]*)"""")
        
        argPattern.findAll(argsString).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2]
            args[key] = value
        }
        
        return args
    }
    
    /**
     * Format tool result for display
     */
    private fun formatToolResult(result: ToolCallResult): String {
        if (result.isError) {
            return "[Error: ${result.content.firstOrNull()?.text ?: "Unknown error"}]"
        }
        
        return result.content.joinToString("\n") { item ->
            when (item.type) {
                "text" -> item.text ?: ""
                "image" -> "[Image: ${item.mimeType}]"
                "resource" -> "[Resource: ${item.mimeType}]"
                else -> item.text ?: ""
            }
        }
    }
}
