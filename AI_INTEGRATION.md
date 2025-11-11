# AI Integration Update

This update replaces the Google Gemini AI integration with Pollinations AI, adding new features:

## Features

### 1. Pollinations AI Integration
- **Anonymous Models**: Use AI models without requiring an API key
- **API Key Support**: Optional API key for premium models
- **Model Selection**: Dynamically fetches available models from Pollinations API
- **Chat Interface**: Chat with AI assistant for code help and general questions

### 2. Inline Code Copilot
- **Real-time Suggestions**: Get AI-powered code completions as you type
- **Context-Aware**: Uses surrounding code to provide relevant suggestions
- **Language Support**: Supports Java and Kotlin
- **Easy Accept**: Press Tab to accept the suggestion
- **Configurable Delay**: Adjust how long to wait before requesting suggestions

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

## Using the Inline Copilot

1. Enable copilot in Settings → AI Assistant
2. Start typing code in the editor
3. After a short delay, you'll see a toast notification when a suggestion is available
4. Press **Tab** to accept the suggestion
5. Continue typing to dismiss it

## Using the Chat Interface

1. Click the chat icon in the toolbar
2. Type your question or request
3. Get AI-powered responses
4. Use for code explanations, debugging help, or general programming questions

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
