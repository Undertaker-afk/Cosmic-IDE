/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * MCP Client implementation for communicating with MCP servers over HTTP
 */
class McpClient(
    private val serverUrl: String,
    private val timeout: Long = 30
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private var initialized = false
    private var serverCapabilities: McpCapabilities? = null
    private var availableTools: List<McpTool> = emptyList()

    /**
     * Initialize the MCP connection
     */
    suspend fun initialize(): McpInitializeResult {
        val request = JsonRpcRequest(
            id = UUID.randomUUID().toString(),
            method = "initialize",
            params = mapOf(
                "protocolVersion" to "2024-11-05",
                "capabilities" to mapOf(
                    "roots" to mapOf("listChanged" to true),
                    "sampling" to emptyMap<String, Any>()
                ),
                "clientInfo" to mapOf(
                    "name" to "Cosmic IDE",
                    "version" to "2.0.5"
                )
            )
        )

        val response = sendRequest(request)
        
        if (response.error != null) {
            throw McpException("Initialization failed: ${response.error.message}")
        }

        val result = gson.fromJson(
            gson.toJson(response.result),
            McpInitializeResult::class.java
        )
        
        serverCapabilities = result.capabilities
        initialized = true
        
        // Fetch available tools if supported
        if (result.capabilities.tools != null) {
            fetchTools()
        }
        
        return result
    }

    /**
     * List available tools from the server
     */
    suspend fun listTools(): List<McpTool> {
        ensureInitialized()
        
        val request = JsonRpcRequest(
            id = UUID.randomUUID().toString(),
            method = "tools/list",
            params = null
        )

        val response = sendRequest(request)
        
        if (response.error != null) {
            throw McpException("Failed to list tools: ${response.error.message}")
        }

        val toolsArray = response.result?.get("tools") as? List<*>
        return toolsArray?.mapNotNull { tool ->
            gson.fromJson(gson.toJson(tool), McpTool::class.java)
        } ?: emptyList()
    }

    /**
     * Call a tool on the MCP server
     */
    suspend fun callTool(toolName: String, arguments: Map<String, Any>): ToolCallResult {
        ensureInitialized()
        
        val request = JsonRpcRequest(
            id = UUID.randomUUID().toString(),
            method = "tools/call",
            params = mapOf(
                "name" to toolName,
                "arguments" to arguments
            )
        )

        val response = sendRequest(request)
        
        if (response.error != null) {
            return ToolCallResult(
                content = listOf(
                    ContentItem(
                        type = "text",
                        text = "Error calling tool: ${response.error.message}"
                    )
                ),
                isError = true
            )
        }

        return gson.fromJson(
            gson.toJson(response.result),
            ToolCallResult::class.java
        )
    }

    /**
     * List available resources
     */
    suspend fun listResources(): List<McpResource> {
        ensureInitialized()
        
        val request = JsonRpcRequest(
            id = UUID.randomUUID().toString(),
            method = "resources/list",
            params = null
        )

        val response = sendRequest(request)
        
        if (response.error != null) {
            throw McpException("Failed to list resources: ${response.error.message}")
        }

        val resourcesArray = response.result?.get("resources") as? List<*>
        return resourcesArray?.mapNotNull { resource ->
            gson.fromJson(gson.toJson(resource), McpResource::class.java)
        } ?: emptyList()
    }

    /**
     * Read a resource
     */
    suspend fun readResource(uri: String): String {
        ensureInitialized()
        
        val request = JsonRpcRequest(
            id = UUID.randomUUID().toString(),
            method = "resources/read",
            params = mapOf("uri" to uri)
        )

        val response = sendRequest(request)
        
        if (response.error != null) {
            throw McpException("Failed to read resource: ${response.error.message}")
        }

        val contents = response.result?.get("contents") as? List<*>
        val firstContent = contents?.firstOrNull() as? Map<*, *>
        return firstContent?.get("text") as? String ?: ""
    }

    /**
     * Get available tools (cached)
     */
    fun getAvailableTools(): List<McpTool> = availableTools

    /**
     * Check if client is initialized
     */
    fun isInitialized(): Boolean = initialized

    /**
     * Send a JSON-RPC request to the MCP server
     */
    private fun sendRequest(request: JsonRpcRequest): JsonRpcResponse {
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val httpRequest = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(httpRequest).execute()
        val responseBody = response.body?.string() 
            ?: throw McpException("Empty response from server")

        if (!response.isSuccessful) {
            throw McpException("HTTP Error: ${response.code} - ${response.message}")
        }

        return gson.fromJson(responseBody, JsonRpcResponse::class.java)
    }

    /**
     * Ensure the client is initialized
     */
    private fun ensureInitialized() {
        if (!initialized) {
            throw McpException("MCP client not initialized. Call initialize() first.")
        }
    }

    /**
     * Fetch and cache available tools
     */
    private suspend fun fetchTools() {
        try {
            availableTools = listTools()
        } catch (e: Exception) {
            // Silently fail, tools will be empty
            availableTools = emptyList()
        }
    }
}

/**
 * MCP Exception
 */
class McpException(message: String, cause: Throwable? = null) : Exception(message, cause)
