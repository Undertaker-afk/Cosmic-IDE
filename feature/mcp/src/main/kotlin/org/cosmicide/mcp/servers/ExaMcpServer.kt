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
 * Exa AI MCP Server integration
 * Provides web search and research capabilities
 */
class ExaMcpServer {
    private val client = McpClient("https://mcp.exa.ai")
    
    suspend fun initialize() {
        client.initialize()
    }
    
    /**
     * Search the web using Exa
     */
    suspend fun search(query: String, numResults: Int = 10): ToolCallResult {
        return client.callTool(
            "search",
            mapOf(
                "query" to query,
                "numResults" to numResults
            )
        )
    }
    
    /**
     * Find similar content
     */
    suspend fun findSimilar(url: String, numResults: Int = 10): ToolCallResult {
        return client.callTool(
            "findSimilar",
            mapOf(
                "url" to url,
                "numResults" to numResults
            )
        )
    }
    
    /**
     * Get page contents
     */
    suspend fun getContents(url: String): ToolCallResult {
        return client.callTool(
            "getContents",
            mapOf("url" to url)
        )
    }
    
    fun isInitialized() = client.isInitialized()
}
