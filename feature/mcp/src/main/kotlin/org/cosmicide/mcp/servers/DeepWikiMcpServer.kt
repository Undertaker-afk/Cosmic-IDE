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
 * DeepWiki MCP Server integration
 * Provides knowledge base and documentation search
 */
class DeepWikiMcpServer {
    private val client = McpClient("https://mcp.deepwiki.com")
    
    suspend fun initialize() {
        client.initialize()
    }
    
    /**
     * Search knowledge base
     */
    suspend fun search(query: String, maxResults: Int = 10): ToolCallResult {
        return client.callTool(
            "search",
            mapOf(
                "query" to query,
                "maxResults" to maxResults
            )
        )
    }
    
    /**
     * Get article content
     */
    suspend fun getArticle(articleId: String): ToolCallResult {
        return client.callTool(
            "getArticle",
            mapOf("articleId" to articleId)
        )
    }
    
    /**
     * Search programming documentation
     */
    suspend fun searchDocs(
        query: String,
        language: String? = null,
        framework: String? = null
    ): ToolCallResult {
        val args = mutableMapOf<String, Any>("query" to query)
        language?.let { args["language"] = it }
        framework?.let { args["framework"] = it }
        
        return client.callTool("searchDocs", args)
    }
    
    fun isInitialized() = client.isInitialized()
}
