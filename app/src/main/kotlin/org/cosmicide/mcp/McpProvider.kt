/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cosmicide.mcp.McpManager
import java.io.File

/**
 * Provider for MCP functionality in the app
 */
object McpProvider {
    
    private const val TAG = "McpProvider"
    private var mcpManager: McpManager? = null
    private var initialized = false
    
    /**
     * Initialize MCP with project context
     */
    fun initialize(context: Context, projectPath: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rootPath = projectPath ?: getDefaultProjectPath(context)
                mcpManager = McpManager(rootPath)
                mcpManager?.initialize()
                initialized = true
                Log.d(TAG, "MCP initialized with project: $rootPath")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MCP", e)
            }
        }
    }
    
    /**
     * Get the MCP manager instance
     */
    fun getManager(): McpManager? = mcpManager
    
    /**
     * Check if MCP is initialized
     */
    fun isInitialized(): Boolean = initialized
    
    /**
     * Set project path dynamically
     */
    fun setProjectPath(projectPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mcpManager = McpManager(projectPath)
                mcpManager?.initialize()
                initialized = true
                Log.d(TAG, "MCP re-initialized with project: $projectPath")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to re-initialize MCP", e)
            }
        }
    }
    
    /**
     * Get default project path
     */
    private fun getDefaultProjectPath(context: Context): String {
        // Try to find a projects directory
        val externalDir = context.getExternalFilesDir(null)
        val projectsDir = File(externalDir, "projects")
        
        return if (projectsDir.exists()) {
            projectsDir.absolutePath
        } else {
            externalDir?.absolutePath ?: context.filesDir.absolutePath
        }
    }
    
    /**
     * Build enhanced AI prompt with MCP context
     */
    suspend fun buildEnhancedPrompt(
        userMessage: String,
        currentFilePath: String? = null,
        includeCodebaseContext: Boolean = true
    ): String {
        return mcpManager?.buildAIPrompt(userMessage, currentFilePath, includeCodebaseContext)
            ?: userMessage
    }
    
    /**
     * Get available tools description
     */
    fun getToolsDescription(): String {
        return mcpManager?.getAvailableToolsDescription() ?: ""
    }
}
