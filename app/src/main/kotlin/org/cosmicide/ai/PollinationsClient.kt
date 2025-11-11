/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Client for interacting with the Pollinations AI API
 * API Documentation: https://text.pollinations.ai/openai
 */
object PollinationsClient {

    private const val TAG = "PollinationsClient"
    private const val BASE_URL = "https://text.pollinations.ai"
    private const val MODELS_ENDPOINT = "$BASE_URL/models"
    private const val COMPLETIONS_ENDPOINT = "$BASE_URL/openai/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    data class Model(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String? = null,
        @SerializedName("requiresAuth") val requiresAuth: Boolean = false
    )

    data class ChatMessage(
        @SerializedName("role") val role: String,
        @SerializedName("content") val content: String
    )

    data class ChatCompletionRequest(
        @SerializedName("model") val model: String,
        @SerializedName("messages") val messages: List<ChatMessage>,
        @SerializedName("temperature") val temperature: Float? = null,
        @SerializedName("max_tokens") val maxTokens: Int? = null,
        @SerializedName("top_p") val topP: Float? = null,
        @SerializedName("stream") val stream: Boolean = false
    )

    data class ChatCompletionResponse(
        @SerializedName("id") val id: String,
        @SerializedName("object") val objectType: String,
        @SerializedName("created") val created: Long,
        @SerializedName("model") val model: String,
        @SerializedName("choices") val choices: List<Choice>
    )

    data class Choice(
        @SerializedName("index") val index: Int,
        @SerializedName("message") val message: ChatMessage,
        @SerializedName("finish_reason") val finishReason: String?
    )

    /**
     * Fetch available models from the Pollinations API
     */
    fun fetchModels(): List<Model> {
        return try {
            val request = Request.Builder()
                .url(MODELS_ENDPOINT)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return getDefaultModels()
            
            if (!response.isSuccessful) {
                Log.w(TAG, "Failed to fetch models: ${response.code}")
                return getDefaultModels()
            }

            val models = gson.fromJson(body, Array<Model>::class.java).toList()
            if (models.isEmpty()) getDefaultModels() else models
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching models", e)
            getDefaultModels()
        }
    }

    /**
     * Default models in case API is not accessible
     */
    private fun getDefaultModels(): List<Model> {
        return listOf(
            Model("openai", "OpenAI (Anonymous)", "OpenAI models without API key", false),
            Model("mistral", "Mistral (Anonymous)", "Mistral models without API key", false),
            Model("gpt-4", "GPT-4", "OpenAI GPT-4 (requires API key)", true),
            Model("gpt-3.5-turbo", "GPT-3.5 Turbo", "OpenAI GPT-3.5 Turbo (requires API key)", true)
        )
    }

    /**
     * Generate chat completion
     */
    fun generateChatCompletion(
        model: String,
        messages: List<ChatMessage>,
        apiKey: String? = null,
        temperature: Float? = null,
        maxTokens: Int? = null,
        topP: Float? = null
    ): String {
        return try {
            val requestData = ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens,
                topP = topP,
                stream = false
            )

            val jsonBody = gson.toJson(requestData)
            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

            val requestBuilder = Request.Builder()
                .url(COMPLETIONS_ENDPOINT)
                .post(requestBody)

            // Add API key header if provided
            if (!apiKey.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return "Error: Empty response"

            if (!response.isSuccessful) {
                Log.e(TAG, "API Error: ${response.code} - $responseBody")
                return "Error: ${response.code} - ${response.message}"
            }

            val completion = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
            completion.choices.firstOrNull()?.message?.content ?: "Error: No response generated"
        } catch (e: Exception) {
            Log.e(TAG, "Error generating completion", e)
            "Error: ${e.message}"
        }
    }

    /**
     * Generate inline code completion
     */
    fun generateCodeCompletion(
        code: String,
        language: String,
        model: String,
        apiKey: String? = null
    ): String {
        val prompt = """
            Complete the following $language code. Provide only the completion, no explanation:
            
            $code
        """.trimIndent()

        val messages = listOf(
            ChatMessage("system", "You are a code completion assistant. Only provide the code completion without any explanations or markdown formatting."),
            ChatMessage("user", prompt)
        )

        return generateChatCompletion(
            model = model,
            messages = messages,
            apiKey = apiKey,
            temperature = 0.2f,
            maxTokens = 150
        )
    }
}
