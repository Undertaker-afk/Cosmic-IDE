/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cosmicide.common.Prefs

/**
 * Provider for AI chat functionality using Pollinations AI
 */
object PollinationsProvider {

    private const val TAG = "PollinationsProvider"

    /**
     * Generate a response for a conversation
     */
    suspend fun generate(conversation: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<PollinationsClient.ChatMessage>()
            
            // Convert conversation history to messages
            conversation.forEach { (author, text) ->
                val role = if (author == "user") "user" else "assistant"
                messages.add(PollinationsClient.ChatMessage(role, text))
            }

            Log.d(TAG, "Generating response with ${messages.size} messages")

            PollinationsClient.generateChatCompletion(
                model = Prefs.aiModel,
                messages = messages,
                apiKey = Prefs.aiApiKey.ifEmpty { null },
                temperature = Prefs.temperature,
                maxTokens = Prefs.maxTokens,
                topP = Prefs.topP
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            "Error: ${e.message}"
        }
    }

    /**
     * Generate inline code completion
     */
    suspend fun generateCodeCompletion(
        code: String,
        language: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (!Prefs.copilotEnabled) {
                return@withContext ""
            }

            Log.d(TAG, "Generating code completion for $language")

            PollinationsClient.generateCodeCompletion(
                code = code,
                language = language,
                model = Prefs.aiModel,
                apiKey = Prefs.aiApiKey.ifEmpty { null }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating code completion", e)
            ""
        }
    }

    /**
     * Fetch available models
     */
    suspend fun fetchModels(): List<PollinationsClient.Model> = withContext(Dispatchers.IO) {
        try {
            PollinationsClient.fetchModels()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching models", e)
            emptyList()
        }
    }
}
