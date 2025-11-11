/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.fragment.settings

import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.helpers.editText
import de.Maxr1998.modernpreferences.helpers.pref
import de.Maxr1998.modernpreferences.helpers.seekBar
import de.Maxr1998.modernpreferences.helpers.singleChoice
import de.Maxr1998.modernpreferences.helpers.switch
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cosmicide.R
import org.cosmicide.ai.PollinationsClient
import org.cosmicide.ai.PollinationsProvider
import org.cosmicide.common.Prefs
import org.cosmicide.fragment.SettingsFragment
import org.cosmicide.util.PreferenceKeys

class AISettings(private val activity: FragmentActivity) : SettingsProvider {

    private val temperature: List<Float>
        get() = listOf(
            0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f,
            0.6f, 0.7f, 0.8f, 0.9f, 1.0f
        )

    private val temperatureKeys = temperature.map { SelectionItem(it.toString(), it.toString(), null) }

    override fun provideSettings(builder: PreferenceScreen.Builder) {
        builder.apply {
            icon = ResourcesCompat.getDrawable(
                activity.resources,
                R.drawable.outline_forum_24,
                activity.theme
            )

            // Add refresh button at the top
            pref("refresh_ai_models") {
                title = "Refresh Available Models"
                summary = "Fetch the latest models from Pollinations AI"
                iconSpaceReserved = false
                onClick {
                    activity.lifecycleScope.launch {
                        try {
                            // Show loading toast
                            withContext(Dispatchers.Main) {
                                Toast.makeText(activity, "Fetching models...", Toast.LENGTH_SHORT).show()
                            }
                            
                            // Fetch models on IO thread
                            val models = withContext(Dispatchers.IO) {
                                PollinationsProvider.fetchModels()
                            }
                            
                            // Save to cache
                            if (models.isNotEmpty()) {
                                Prefs.cachedModels = Gson().toJson(models)
                                
                                // Recreate the fragment to rebuild preferences
                                withContext(Dispatchers.Main) {
                                    activity.supportFragmentManager.beginTransaction()
                                        .replace(org.cosmicide.R.id.fragment_container, SettingsFragment())
                                        .addToBackStack(null)
                                        .commit()
                                    
                                    Toast.makeText(
                                        activity,
                                        "Fetched ${models.size} models successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        activity,
                                        "No models returned from API",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    activity,
                                    "Failed to fetch models: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Log.e("AISettings", "Failed to fetch models", e)
                        }
                    }
                    true
                }
            }

            // Load cached or default models
            val modelKeys = try {
                if (Prefs.cachedModels.isNotEmpty()) {
                    val models = Gson().fromJson(
                        Prefs.cachedModels,
                        Array<PollinationsClient.Model>::class.java
                    ).toList()
                    
                    models.map { model ->
                        SelectionItem(
                            model.id,
                            model.name,
                            if (model.requiresAuth) "Requires API key" else "Anonymous model"
                        )
                    }
                } else {
                    getDefaultModelList()
                }
            } catch (e: Exception) {
                Log.e("AISettings", "Failed to parse cached models", e)
                getDefaultModelList()
            }

            singleChoice(PreferenceKeys.AI_MODEL, modelKeys) {
                title = "AI Model"
                summary = if (Prefs.cachedModels.isNotEmpty()) {
                    "Showing ${modelKeys.size} models (tap Refresh to update)"
                } else {
                    "Showing default models (tap Refresh to fetch all)"
                }
                initialSelection = "openai"
                iconSpaceReserved = false
            }

            editText(PreferenceKeys.AI_API_KEY) {
                title = "API Key (Optional)"
                summary = "API key for models that require authentication. Leave empty for anonymous models."
                textInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconSpaceReserved = false
            }

            switch(PreferenceKeys.COPILOT_ENABLED) {
                title = "Enable Inline Code Copilot"
                summary = "Show AI-powered code completion suggestions while typing"
                defaultValue = true
                iconSpaceReserved = false
            }

            seekBar(PreferenceKeys.COPILOT_DELAY) {
                title = "Copilot Delay (ms)"
                summary = "Delay before showing inline suggestions after typing stops"
                max = 5000
                min = 100
                default = 500
                iconSpaceReserved = false
            }

            singleChoice(PreferenceKeys.TEMPERATURE, temperatureKeys) {
                title = "Temperature"
                summary = "Controls randomness. Higher values (0.8-1.0) are more creative, lower values (0.0-0.2) are more focused and deterministic."
                initialSelection = "0.7"
                iconSpaceReserved = false
            }

            singleChoice(PreferenceKeys.TOP_P, temperatureKeys) {
                title = "Top P"
                summary = "Controls diversity via nucleus sampling. Lower values make output more focused."
                initialSelection = "1.0"
                iconSpaceReserved = false
            }

            seekBar(PreferenceKeys.TOP_K) {
                title = "Top K"
                summary = "Limits token sampling to top K most likely tokens."
                max = 60
                min = 1
                default = 40
                iconSpaceReserved = false
            }

            seekBar(PreferenceKeys.MAX_TOKENS) {
                title = "Max Tokens"
                summary = "Maximum number of tokens to generate in responses."
                max = 2048
                min = 60
                default = 1024
                iconSpaceReserved = false
            }
        }
    }

    private fun getDefaultModelList(): List<SelectionItem> {
        return listOf(
            SelectionItem("openai", "OpenAI", "Anonymous model"),
            SelectionItem("mistral", "Mistral", "Anonymous model"),
            SelectionItem("gpt-4", "GPT-4", "Requires API key"),
            SelectionItem("gpt-3.5-turbo", "GPT-3.5 Turbo", "Requires API key")
        )
    }
}
