/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.editor

import android.os.Handler
import android.os.Looper
import android.util.Log
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
     */
    fun acceptSuggestion() {
        currentSuggestion?.let { suggestion ->
            if (suggestion.isNotEmpty()) {
                editor.commitText(suggestion)
                clearSuggestion()
            }
        }
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
        // TODO: Update UI to hide the suggestion
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
                        // TODO: Display suggestion in UI (e.g., as ghost text or in a tooltip)
                        Log.d(TAG, "Got suggestion: $currentSuggestion")
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
