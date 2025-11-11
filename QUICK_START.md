# Quick Start Guide - Pollinations AI & Code Copilot

## For End Users

### Enable AI Features

1. Open **Cosmic IDE**
2. Go to **Settings** (gear icon)
3. Tap **AI Assistant**
4. Choose a model (e.g., "openai" for anonymous use)
5. (Optional) Enter an API key for premium models

### Use Inline Code Copilot

1. In Settings → AI Assistant:
   - Enable "Inline Code Copilot"
   - Adjust delay if needed (default: 500ms)

2. While coding:
   - Type code as normal
   - Wait for "Code suggestion available" toast
   - Press **Tab** to accept suggestion
   - Continue typing to dismiss

### Use AI Chat

1. Open a project
2. Tap the chat icon in the toolbar
3. Type your question
4. Get AI-powered responses

## For Developers

### Architecture Overview

```
IdeEditor → InlineCopilot → PollinationsProvider → PollinationsClient → API
ChatFragment → PollinationsProvider → PollinationsClient → API
AISettings → PollinationsProvider (model fetching)
```

### Key Classes

**PollinationsClient** (`org.cosmicide.ai`)
- HTTP client for Pollinations API
- Methods: `fetchModels()`, `generateChatCompletion()`, `generateCodeCompletion()`

**PollinationsProvider** (`org.cosmicide.ai`)
- High-level facade with coroutines
- Methods: `generate()`, `generateCodeCompletion()`, `fetchModels()`

**InlineCopilot** (`org.cosmicide.editor`)
- Manages code completion lifecycle
- Methods: `requestCompletion()`, `acceptSuggestion()`, `release()`

**AISettings** (`org.cosmicide.fragment.settings`)
- UI for AI configuration
- Dynamic model loading

### Adding a New AI Provider

To add another AI provider:

1. Create a new client class (similar to `PollinationsClient`)
2. Update `PollinationsProvider` or create a new provider
3. Update `AISettings` for new configuration options
4. Update `Prefs` if new preferences needed

### Customizing Copilot Behavior

Edit `InlineCopilot.kt`:

```kotlin
// Change context size (default: 500 chars)
val contextStart = maxOf(0, cursorIndex - 1000)

// Change prompt template
val prompt = """Your custom prompt here"""

// Add language support
when (editor.editorLanguage?.javaClass?.simpleName) {
    "YourLanguage" -> "LanguageName"
    // ...
}
```

### API Configuration

Edit `PollinationsClient.kt`:

```kotlin
private const val BASE_URL = "https://your-api-endpoint"
private const val MODELS_ENDPOINT = "$BASE_URL/models"
private const val COMPLETIONS_ENDPOINT = "$BASE_URL/completions"
```

## Testing

### Manual Testing Checklist

- [ ] Open Settings → AI Assistant
- [ ] Verify model list appears
- [ ] Select a model
- [ ] Enable copilot
- [ ] Type code in editor
- [ ] See toast notification
- [ ] Press Tab to accept
- [ ] Open AI Chat
- [ ] Send a message
- [ ] Receive response

### Common Issues

**No suggestions appearing:**
- Check copilot is enabled in settings
- Check delay setting (may need to wait longer)
- Check logs for API errors

**API errors:**
- Verify internet connection
- Check API endpoint is accessible
- For premium models, verify API key is correct

**Build errors:**
- Pre-existing AGP version issue (not from this PR)
- Wait for base repository fix

## Configuration Examples

### Anonymous Mode (No API Key)
```
Model: openai
API Key: (empty)
Copilot Enabled: Yes
Delay: 500ms
Temperature: 0.7
```

### Premium Mode (With API Key)
```
Model: gpt-4
API Key: sk-xxxxx...
Copilot Enabled: Yes
Delay: 300ms
Temperature: 0.9
```

### Conservative Settings
```
Temperature: 0.2 (focused)
Top P: 0.5 (less diverse)
Top K: 10 (narrow selection)
Max Tokens: 512 (shorter responses)
```

### Creative Settings
```
Temperature: 0.9 (creative)
Top P: 1.0 (diverse)
Top K: 60 (wide selection)
Max Tokens: 2048 (longer responses)
```

## Resources

- **User Guide**: See `AI_INTEGRATION.md`
- **Technical Details**: See `IMPLEMENTATION_SUMMARY.md`
- **API Docs**: https://text.pollinations.ai/
- **Source Code**: `app/src/main/kotlin/org/cosmicide/ai/`

## Support

For issues or questions:
1. Check the documentation files
2. Review logs for error messages
3. Open an issue on GitHub
4. Join the Cosmic IDE community (Discord/Telegram)
