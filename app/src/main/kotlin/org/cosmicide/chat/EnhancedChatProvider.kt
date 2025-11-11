/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.chat

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cosmicide.ai.PollinationsProvider
import org.cosmicide.common.Prefs
import org.cosmicide.mcp.McpProvider
import org.cosmicide.mcp.McpToolExecutor

/**
 * Enhanced chat provider with MCP support
 */
object EnhancedChatProvider {
    
    private const val TAG = "EnhancedChatProvider"
    
    /**
     * Generate AI response with MCP context and tool execution
     */
    suspend fun generate(
        conversation: List<Pair<String, String>>,
        currentFilePath: String? = null,
        enableMcpContext: Boolean = Prefs.mcpEnabled,
        enableToolExecution: Boolean = Prefs.mcpToolsEnabled
    ): String = withContext(Dispatchers.IO) {
        try {
            val userMessage = conversation.lastOrNull()?.second ?: return@withContext ""
            
            // Build enhanced prompt with MCP context if enabled
            val enhancedMessage = if (enableMcpContext && McpProvider.isInitialized()) {
                val systemPrompt = buildSystemPrompt(enableToolExecution)
                val contextualMessage = McpProvider.buildEnhancedPrompt(
                    userMessage,
                    currentFilePath,
                    includeCodebaseContext = true
                )
                
                // Prepend system prompt to conversation
                listOf(
                    Pair("system", systemPrompt),
                    Pair("user", contextualMessage)
                ) + conversation.dropLast(1)
            } else {
                conversation
            }
            
            // Generate AI response
            val response = PollinationsProvider.generate(enhancedMessage)
            
            // Execute tool calls if enabled
            if (enableToolExecution && McpProvider.isInitialized()) {
                McpToolExecutor.executeToolCalls(response, currentFilePath)
            } else {
                response
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            "Error: ${e.message}"
        }
    }
    
    /**
     * Build system prompt with tool information
     */
    private fun buildSystemPrompt(includeTools: Boolean): String {
        val builder = StringBuilder()
        
        builder.appendLine("You are an AI coding assistant integrated into Cosmic IDE.")
        builder.appendLine("You help developers write, understand, and debug code.")
        builder.appendLine()
        
        if (includeTools && McpProvider.isInitialized()) {
            builder.appendLine("You have access to the following tools:")
            builder.appendLine(McpProvider.getToolsDescription())
            builder.appendLine()
            builder.appendLine("To use a tool, wrap the call in <tool></tool> tags:")
            builder.appendLine("Example: <tool>read_file(path=\"src/Main.kt\")</tool>")
            builder.appendLine()
        }
        
        builder.appendLine("Always provide clear, concise, and helpful responses.")
        builder.appendLine("When suggesting code changes, explain what the code does.")
        
        return builder.toString()
    }
    
    /**
     * Generate code completion with context
     */
    suspend fun generateCodeCompletion(
        code: String,
        language: String,
        currentFilePath: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            // Add codebase context if available
            val contextualCode = if (McpProvider.isInitialized() && currentFilePath != null) {
                val context = McpProvider.getManager()?.getCodebaseContext(currentFilePath)
                val imports = context?.imports?.joinToString("\n") ?: ""
                "$imports\n\n$code"
            } else {
                code
            }
            
            PollinationsProvider.generateCodeCompletion(contextualCode, language)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating code completion", e)
            ""
        }
    }
}
