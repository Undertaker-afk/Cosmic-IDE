package org.cosmicide.editor

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cosmicide.ai.PollinationsProvider
import org.cosmicide.common.Prefs

/**
 * Provides inline code completion suggestions similar to GitHub Copilot
 */
class InlineCopilot(private val editor: CodeEditor) {

    private val TAG = "InlineCopilot"
    private val handler = Handler(Looper.getMainLooper())
    private var completionJob: Job? = null
    private var currentSuggestion: String? = null
    private var suggestionRunnable: Runnable? = null

    /**
     * Request a code completion at the current cursor position
     */
    fun requestCompletion() {
        if (!Prefs.copilotEnabled) {
            return
        }

        // Cancel any pending completion requests
        cancelPendingCompletion()

        // Schedule a new completion request with a delay
        suggestionRunnable = Runnable {
            performCompletion()
        }
        handler.postDelayed(suggestionRunnable!!, Prefs.copilotDelay.toLong())
    }

    /**
     * Cancel any pending completion requests
     */
    fun cancelPendingCompletion() {
        suggestionRunnable?.let {
            handler.removeCallbacks(it)
        }
        completionJob?.cancel()
    }

    /**
     * Accept the current suggestion
     * @return true if a suggestion was accepted, false otherwise
     */
    fun acceptSuggestion(): Boolean {
        currentSuggestion?.let { suggestion ->
            if (suggestion.isNotEmpty()) {
                editor.commitText(suggestion)
                clearSuggestion()
                return true
            }
        }
        return false
    }

    /**
     * Reject the current suggestion
     */
    fun rejectSuggestion() {
        clearSuggestion()
    }

    /**
     * Clear the current suggestion
     */
    private fun clearSuggestion() {
        currentSuggestion = null
    }

    /**
     * Perform the actual completion request
     */
    private fun performCompletion() {
        completionJob?.cancel()
        completionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val text = editor.text.toString()
                val cursor = editor.cursor
                val cursorIndex = editor.text.getCharIndex(cursor.leftLine, cursor.leftColumn)
                
                // Get context before cursor (last 500 characters or less)
                val contextStart = maxOf(0, cursorIndex - 500)
                val context = text.substring(contextStart, cursorIndex)

                // Determine language
                val language = determineLanguage()

                Log.d(TAG, "Requesting completion for $language")

                // Request completion from AI
                val suggestion = PollinationsProvider.generateCodeCompletion(context, language)

                withContext(Dispatchers.Main) {
                    if (suggestion.isNotEmpty() && !suggestion.startsWith("Error:")) {
                        currentSuggestion = suggestion.trim()
                        Log.d(TAG, "Got suggestion: $currentSuggestion")
                        // Show a subtle toast to indicate suggestion is available
                        Toast.makeText(editor.context, "Code suggestion available (press Tab to accept)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting completion", e)
            }
        }
    }

    /**
     * Determine the programming language based on editor context
     */
    private fun determineLanguage(): String {
        return when (editor.editorLanguage?.javaClass?.simpleName) {
            "TsLanguageJava" -> "Java"
            "KotlinLanguage" -> "Kotlin"
            else -> "Java" // Default to Java
        }
    }

    /**
     * Release resources
     */
    fun release() {
        cancelPendingCompletion()
        handler.removeCallbacksAndMessages(null)
    }
}
