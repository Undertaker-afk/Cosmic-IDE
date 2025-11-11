# MCP (Model Context Protocol) Integration

## Overview

Cosmic IDE now supports the Model Context Protocol (MCP), enabling powerful AI-assisted coding with full codebase awareness and external tool integration.

## Features

### 1. Codebase Context Engine
- **Kotlin Code Analysis**: Automatically analyzes Kotlin source files to extract:
  - Classes, interfaces, and objects
  - Functions with parameters and return types
  - Properties and their types
  - Package structure
  - Modifiers and visibility
- **Project Structure Analysis**: Maps the entire project structure including modules, dependencies, and file organization
- **Context Generation**: Provides AI with relevant context about the current file and related files

### 2. MCP Server Integration

#### Exa AI (mcp.exa.ai)
Search and research capabilities:
- `search(query, numResults)`: Web search
- `findSimilar(url, numResults)`: Find similar content
- `getContents(url)`: Get page contents

#### Grep.app (mcp.grep.app)
Code search on GitHub:
- `searchCode(query, language, repo)`: Search code
- `searchInRepo(repo, query, language)`: Search in specific repository
- `getFile(repo, path, ref)`: Get file content

#### DeepWiki (mcp.deepwiki.com)
Knowledge base and documentation:
- `search(query, maxResults)`: Search knowledge base
- `getArticle(articleId)`: Get article content
- `searchDocs(query, language, framework)`: Search programming documentation

### 3. File Operations
The AI can read and edit files in your project:
- `read_file(path)`: Read file content
- `write_file(path, content)`: Write to file
- `list_files(path)`: List directory contents
- `get_codebase_context(file_path)`: Get project context

## Configuration

Access MCP settings from: **Settings → AI Assistant**

### Available Settings

#### MCP Settings
- **Enable MCP Context**: Use codebase context in AI responses (default: on)
- **Enable MCP Tools**: Allow AI to execute tools (default: on)
- **Enable Exa AI Search**: Web search via mcp.exa.ai (default: on)
- **Enable Grep.app Code Search**: GitHub search via mcp.grep.app (default: on)
- **Enable DeepWiki Knowledge Base**: Documentation search via mcp.deepwiki.com (default: on)

## Using MCP Features

### In Chat
1. Open the AI Chat from the toolbar
2. Ask questions about your codebase - the AI automatically has context
3. Request file operations:
   - "Show me the contents of MainActivity.kt"
   - "Edit MyClass.kt to add a new function"
4. Request external searches:
   - "Search for Kotlin coroutines examples"
   - "Find similar implementations on GitHub"

### Tool Execution
The AI can execute tools using the following syntax in its responses:
```
<tool>tool_name(arg1="value1", arg2="value2")</tool>
```

Example AI response with tool execution:
```
I'll check the file content for you.
<tool>read_file(path="src/main/kotlin/MainActivity.kt")</tool>

Based on the file, I can suggest...
```

### Context-Aware Code Completion
The inline copilot now uses codebase context:
1. Enable "Enable Inline Code Copilot" in settings
2. Start typing in the editor
3. Completions include awareness of:
   - Imports in the current file
   - Classes and functions in the project
   - Project structure and conventions

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Chat UI / Editor                  │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│              EnhancedChatProvider                    │
│  • Builds prompts with codebase context             │
│  • Executes tool calls from AI responses            │
└─────────────────────┬───────────────────────────────┘
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
┌─────────────────────┐  ┌─────────────────────┐
│    McpManager       │  │  PollinationsAI     │
│  • Server coord.    │  │  • Text generation  │
│  • Context engine   │  │  • Code completion  │
│  • Tool dispatch    │  │                     │
└──────┬──────────────┘  └─────────────────────┘
       │
       ├── CodebaseContextEngine
       │   ├── KotlinCodeAnalyzer
       │   ├── ProjectStructureAnalyzer
       │   └── FileOperations
       │
       └── MCP Servers
           ├── ExaMcpServer (mcp.exa.ai)
           ├── GrepMcpServer (mcp.grep.app)
           └── DeepWikiMcpServer (mcp.deepwiki.com)
```

## Technical Details

### MCP Protocol
- **Transport**: HTTP with JSON-RPC 2.0
- **Protocol Version**: 2024-11-05
- **Methods**:
  - `initialize`: Establish connection
  - `tools/list`: Get available tools
  - `tools/call`: Execute a tool
  - `resources/list`: List resources
  - `resources/read`: Read resource content

### Codebase Context Engine
The context engine provides:
1. **Current File Context**:
   - File content
   - Imports
   - Classes and functions
   - Language and type

2. **Related Files**:
   - Files in the same package/directory
   - Commonly imported files
   - Related modules

3. **Project Structure**:
   - Module list and types
   - Dependencies
   - Total files and lines
   - Build configuration

### Tool Execution Flow
1. User sends message to AI
2. `EnhancedChatProvider` builds prompt with codebase context
3. AI generates response (may include tool calls)
4. `McpToolExecutor` parses and executes tool calls
5. Tool results are injected back into response
6. Final response is displayed to user

## Security Considerations

### File Operations
- File operations are scoped to the project directory
- Write operations require explicit AI tool calls
- All operations are logged

### MCP Servers
- All MCP servers use HTTPS
- No authentication required for read-only operations
- Tool execution can be disabled in settings

### Privacy
- MCP context is only sent to AI when explicitly enabled
- File contents are not sent to MCP servers without tool calls
- All data transmission uses secure HTTPS

## Troubleshooting

### MCP Not Working
1. Check Settings → AI Assistant → Enable MCP Context is ON
2. Verify internet connection for MCP servers
3. Check logs for initialization errors

### Tool Execution Failing
1. Ensure Settings → AI Assistant → Enable MCP Tools is ON
2. Check specific server settings (Exa, Grep, DeepWiki)
3. Verify file paths are correct and accessible

### Context Not Appearing
1. Open a file in the editor before starting chat
2. Ensure the project is properly initialized
3. Check that MCP initialization completed (see logs)

## API Reference

### McpProvider
```kotlin
// Initialize with context
McpProvider.initialize(context, projectPath)

// Build enhanced prompt
val prompt = McpProvider.buildEnhancedPrompt(
    userMessage = "Help me fix this bug",
    currentFilePath = "/path/to/file.kt",
    includeCodebaseContext = true
)

// Get tools description
val tools = McpProvider.getToolsDescription()
```

### EnhancedChatProvider
```kotlin
// Generate with MCP context
val response = EnhancedChatProvider.generate(
    conversation = listOf(Pair("user", "message")),
    currentFilePath = "/path/to/file.kt",
    enableMcpContext = true,
    enableToolExecution = true
)

// Code completion with context
val completion = EnhancedChatProvider.generateCodeCompletion(
    code = "fun calculate",
    language = "Kotlin",
    currentFilePath = "/path/to/file.kt"
)
```

### McpManager
```kotlin
val manager = McpManager(projectRootPath)
manager.initialize()

// Get context
val context = manager.getCodebaseContext(currentFilePath)

// File operations
val content = manager.readFile(path)
val success = manager.writeFile(path, content)
val files = manager.listFiles(dirPath)

// Search operations
val webResults = manager.searchWeb(query, numResults)
val codeResults = manager.searchCode(query, language, repo)
val docsResults = manager.searchDocs(query, language, framework)
```

## Examples

### Example 1: Context-Aware Bug Fix
```
User: "Fix the null pointer exception in MainActivity.kt"

AI Response:
I'll analyze the file for you.
<tool>read_file(path="app/src/main/kotlin/MainActivity.kt")</tool>

I found the issue on line 42. The `user` variable can be null. 
I'll fix it:
<tool>write_file(path="app/src/main/kotlin/MainActivity.kt", content="...")</tool>

The fix adds a null check using the safe call operator (?.).
```

### Example 2: Code Search and Integration
```
User: "Find examples of RecyclerView adapters on GitHub"

AI Response:
<tool>search_code(query="RecyclerView.Adapter", language="Kotlin")</tool>

Here are some excellent examples... 
Based on your project structure, I recommend...
```

### Example 3: Documentation Search
```
User: "How do I use Kotlin coroutines for network calls?"

AI Response:
<tool>search_docs(query="kotlin coroutines network", language="Kotlin", framework="coroutines")</tool>

Based on the documentation, here's the recommended approach...
```

## Future Enhancements

Potential improvements for future versions:
1. **Additional MCP Servers**: Support for more specialized servers
2. **Custom Tool Definition**: Allow users to define custom tools
3. **Context Caching**: Cache analyzed code structure for faster responses
4. **Multi-Project Support**: Handle multiple open projects
5. **Streaming Tool Results**: Show tool execution progress in real-time
6. **Visual Tool Execution**: UI feedback for tool calls
7. **Tool Call History**: Track and replay tool executions

## Contributing

To add support for additional MCP servers:

1. Create a new server class in `feature/mcp/src/main/kotlin/org/cosmicide/mcp/servers/`
2. Implement the server-specific tool methods
3. Add the server to `McpManager`
4. Add settings in `AISettings.kt`
5. Update this documentation

## License

MCP integration is part of Cosmic IDE and is licensed under GNU GPL-v3.
