/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.mcp

import com.google.gson.annotations.SerializedName

/**
 * MCP (Model Context Protocol) data types
 * Based on the MCP specification for JSON-RPC over HTTP
 */

/**
 * JSON-RPC request
 */
data class JsonRpcRequest(
    @SerializedName("jsonrpc") val jsonrpc: String = "2.0",
    @SerializedName("id") val id: String,
    @SerializedName("method") val method: String,
    @SerializedName("params") val params: Map<String, Any>? = null
)

/**
 * JSON-RPC response
 */
data class JsonRpcResponse(
    @SerializedName("jsonrpc") val jsonrpc: String,
    @SerializedName("id") val id: String?,
    @SerializedName("result") val result: Map<String, Any>?,
    @SerializedName("error") val error: JsonRpcError?
)

/**
 * JSON-RPC error
 */
data class JsonRpcError(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any? = null
)

/**
 * MCP Tool definition
 */
data class McpTool(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("inputSchema") val inputSchema: Map<String, Any>
)

/**
 * MCP Resource definition
 */
data class McpResource(
    @SerializedName("uri") val uri: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("mimeType") val mimeType: String?
)

/**
 * MCP Prompt definition
 */
data class McpPrompt(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("arguments") val arguments: List<Map<String, Any>>?
)

/**
 * MCP Server capabilities
 */
data class McpCapabilities(
    @SerializedName("tools") val tools: Map<String, Any>? = null,
    @SerializedName("resources") val resources: Map<String, Any>? = null,
    @SerializedName("prompts") val prompts: Map<String, Any>? = null
)

/**
 * MCP Initialize result
 */
data class McpInitializeResult(
    @SerializedName("protocolVersion") val protocolVersion: String,
    @SerializedName("capabilities") val capabilities: McpCapabilities,
    @SerializedName("serverInfo") val serverInfo: McpServerInfo
)

/**
 * MCP Server info
 */
data class McpServerInfo(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String
)

/**
 * Tool call result
 */
data class ToolCallResult(
    @SerializedName("content") val content: List<ContentItem>,
    @SerializedName("isError") val isError: Boolean = false
)

/**
 * Content item in tool results
 */
data class ContentItem(
    @SerializedName("type") val type: String, // "text", "image", "resource"
    @SerializedName("text") val text: String? = null,
    @SerializedName("data") val data: String? = null,
    @SerializedName("mimeType") val mimeType: String? = null
)
