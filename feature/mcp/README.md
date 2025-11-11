# MCP Module

This module implements the Model Context Protocol (MCP) for Cosmic IDE, enabling AI-assisted coding with full codebase awareness.

## Structure

```
feature/mcp/
├── build.gradle.kts                 # Module build configuration
└── src/main/kotlin/org/cosmicide/mcp/
    ├── McpTypes.kt                  # MCP protocol data types
    ├── McpClient.kt                 # JSON-RPC HTTP client
    ├── McpManager.kt                # Main MCP coordinator
    ├── context/
    │   ├── ContextTypes.kt          # Code analysis data types
    │   ├── KotlinCodeAnalyzer.kt    # Kotlin source parser
    │   └── CodebaseContextEngine.kt # Project structure analyzer
    └── servers/
        ├── ExaMcpServer.kt          # Exa AI integration
        ├── GrepMcpServer.kt         # Grep.app integration
        └── DeepWikiMcpServer.kt     # DeepWiki integration
```

## Dependencies

- **OkHttp**: HTTP client for MCP server communication
- **Gson**: JSON serialization/deserialization
- **Kotlin Coroutines**: Async operations

## Key Components

### McpClient
Generic MCP protocol client that communicates with any MCP server via HTTP JSON-RPC.

### McpManager
Coordinates all MCP servers and the codebase context engine. Provides a unified API for:
- Getting codebase context
- File operations
- Tool execution across all servers

### Codebase Context Engine
Analyzes Kotlin/Java projects to extract:
- Class/interface/object definitions
- Function signatures and parameters
- Property declarations
- Project structure and modules
- Dependencies

### MCP Server Integrations
Three specialized MCP servers:
- **Exa**: Web search and research
- **Grep.app**: GitHub code search
- **DeepWiki**: Programming documentation search

## Usage

See [MCP_INTEGRATION.md](../../MCP_INTEGRATION.md) for complete documentation.

Basic usage:
```kotlin
val manager = McpManager(projectRootPath)
manager.initialize()

// Get context
val context = manager.getCodebaseContext(currentFilePath)

// Execute tool
val result = manager.searchWeb("kotlin coroutines")
```

## Protocol

Implements MCP specification version 2024-11-05 with:
- JSON-RPC 2.0 transport over HTTP
- Tool discovery and invocation
- Resource listing and reading
- Client/server capability negotiation

## Testing

The module is designed to work with the Cosmic IDE app module integration. Testing requires:
1. Valid project path for context analysis
2. Internet connection for MCP servers
3. Android environment for full integration

## License

Part of Cosmic IDE - GNU GPL-v3
