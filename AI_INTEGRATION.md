# AI Integration Update

This update includes Pollinations AI integration and comprehensive MCP (Model Context Protocol) support for advanced AI-assisted coding.

## Features

### 1. Pollinations AI Integration
- **Anonymous Models**: Use AI models without requiring an API key
- **API Key Support**: Optional API key for premium models
- **Model Selection**: Dynamically fetches available models from Pollinations API
- **Chat Interface**: Chat with AI assistant for code help and general questions

### 2. Inline Code Copilot
- **Real-time Suggestions**: Get AI-powered code completions as you type
- **Context-Aware**: Uses surrounding code and project context to provide relevant suggestions
- **Language Support**: Supports Java and Kotlin
- **Easy Accept**: Press Tab to accept the suggestion
- **Configurable Delay**: Adjust how long to wait before requesting suggestions

### 3. MCP (Model Context Protocol) Support **NEW**
- **Codebase Context Engine**: Automatically analyzes your Kotlin/Java project structure
- **File Operations**: AI can read and edit files in your project
- **Web Search**: Integrated Exa AI for web search and research (mcp.exa.ai)
- **Code Search**: GitHub code search via Grep.app (mcp.grep.app)
- **Knowledge Base**: Programming documentation search via DeepWiki (mcp.deepwiki.com)
- **Tool Execution**: AI can execute tools to gather information and make changes

See [MCP_INTEGRATION.md](MCP_INTEGRATION.md) for detailed MCP documentation.

## Configuration

Access AI settings from: **Settings → AI Assistant**

### Available Settings:
- **AI Model**: Select from available models (anonymous or with API key)
- **API Key**: Optional - only needed for premium models
- **Enable Inline Code Copilot**: Toggle code completion suggestions
- **Copilot Delay**: Adjust delay before showing suggestions (100-5000ms)
- **Temperature**: Control randomness of AI responses (0.0 = focused, 1.0 = creative)
- **Top P**: Control diversity via nucleus sampling
- **Top K**: Limit token sampling
- **Max Tokens**: Maximum length of AI responses

### MCP Settings (NEW):
- **Enable MCP Context**: Provide codebase context to AI (default: on)
- **Enable MCP Tools**: Allow AI to execute tools (default: on)
- **Enable Exa AI Search**: Web search capabilities (default: on)
- **Enable Grep.app Code Search**: GitHub code search (default: on)
- **Enable DeepWiki Knowledge Base**: Documentation search (default: on)

## Using the Inline Copilot

1. Enable copilot in Settings → AI Assistant
2. Start typing code in the editor
3. After a short delay, you'll see a toast notification when a suggestion is available
4. Press **Tab** to accept the suggestion
5. Continue typing to dismiss it

## Using the Chat Interface

1. Click the chat icon in the toolbar
2. Type your question or request
3. Get AI-powered responses with full codebase context
4. AI can automatically:
   - Read your project files
   - Search for code examples
   - Find documentation
   - Edit files to fix issues
5. Use for code explanations, debugging help, or general programming questions

## Using MCP Tools (NEW)

The AI can execute tools to help you:
- **File Operations**: `read_file`, `write_file`, `list_files`
- **Web Search**: Search the web for information
- **Code Search**: Find code examples on GitHub
- **Documentation**: Search programming docs

Example conversation:
```
You: "Fix the bug in MainActivity.kt"
AI: Let me check the file...
    <tool>read_file(path="app/src/main/kotlin/MainActivity.kt")</tool>
    I found the issue. I'll fix it...
    <tool>write_file(path="...", content="...")</tool>
    Done! The null check has been added.
```

## API Endpoints

- **Models**: `https://text.pollinations.ai/models`
- **Completions**: `https://text.pollinations.ai/openai/v1/chat/completions`

## Anonymous vs API Key Models

**Anonymous models** (no API key required):
- openai
- mistral

**Premium models** (API key required):
- gpt-4
- gpt-3.5-turbo

Note: The actual list of available models is fetched dynamically from the Pollinations API.
