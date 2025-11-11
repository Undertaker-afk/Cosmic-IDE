/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp.servers

import org.cosmicide.mcp.McpClient
import org.cosmicide.mcp.ToolCallResult

/**
 * Grep.app MCP Server integration
 * Provides code search across GitHub repositories
 */
class GrepMcpServer {
    private val client = McpClient("https://mcp.grep.app")
    
    suspend fun initialize() {
        client.initialize()
    }
    
    /**
     * Search code across GitHub repositories
     */
    suspend fun searchCode(
        query: String,
        language: String? = null,
        repo: String? = null
    ): ToolCallResult {
        val args = mutableMapOf<String, Any>("query" to query)
        language?.let { args["language"] = it }
        repo?.let { args["repo"] = it }
        
        return client.callTool("search", args)
    }
    
    /**
     * Search in specific repository
     */
    suspend fun searchInRepo(
        repo: String,
        query: String,
        language: String? = null
    ): ToolCallResult {
        val args = mutableMapOf<String, Any>(
            "repo" to repo,
            "query" to query
        )
        language?.let { args["language"] = it }
        
        return client.callTool("searchInRepo", args)
    }
    
    /**
     * Get file content from repository
     */
    suspend fun getFile(repo: String, path: String, ref: String = "main"): ToolCallResult {
        return client.callTool(
            "getFile",
            mapOf(
                "repo" to repo,
                "path" to path,
                "ref" to ref
            )
        )
    }
    
    fun isInitialized() = client.isInitialized()
}
