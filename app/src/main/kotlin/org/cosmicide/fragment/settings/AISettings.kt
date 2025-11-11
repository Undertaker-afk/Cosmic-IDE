/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.fragment.settings

import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.helpers.editText
import de.Maxr1998.modernpreferences.helpers.seekBar
import de.Maxr1998.modernpreferences.helpers.singleChoice
import de.Maxr1998.modernpreferences.helpers.switch
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cosmicide.R
import org.cosmicide.ai.PollinationsProvider
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

            // Model selection - will be populated dynamically
            val modelKeys = mutableListOf<SelectionItem>()
            
            // Fetch models asynchronously
            activity.lifecycleScope.launch {
                val models = withContext(Dispatchers.IO) {
                    PollinationsProvider.fetchModels()
                }
                
                modelKeys.clear()
                models.forEach { model ->
                    val description = if (model.requiresAuth) {
                        "${model.name} (requires API key)"
                    } else {
                        "${model.name} (anonymous)"
                    }
                    modelKeys.add(SelectionItem(model.id, model.name, description))
                }
            }

            singleChoice(PreferenceKeys.AI_MODEL, modelKeys.ifEmpty { 
                listOf(
                    SelectionItem("openai", "OpenAI", "Anonymous model"),
                    SelectionItem("mistral", "Mistral", "Anonymous model")
                )
            }) {
                title = "AI Model"
                summary = "Select the AI model to use. Anonymous models don't require an API key."
                initialSelection = "openai"
            }

            editText(PreferenceKeys.AI_API_KEY) {
                title = "API Key (Optional)"
                summary = "API key for models that require authentication. Leave empty for anonymous models."
            }

            switch(PreferenceKeys.COPILOT_ENABLED) {
                title = "Enable Inline Code Copilot"
                summary = "Show AI-powered code completion suggestions while typing"
                defaultValue = true
            }

            seekBar(PreferenceKeys.COPILOT_DELAY) {
                title = "Copilot Delay (ms)"
                summary = "Delay before showing inline suggestions after typing stops"
                max = 5000
                min = 100
                default = 500
            }

            singleChoice(PreferenceKeys.TEMPERATURE, temperatureKeys) {
                title = "Temperature"
                summary = "Controls randomness. Higher values (0.8-1.0) are more creative, lower values (0.0-0.2) are more focused and deterministic."
                initialSelection = "0.7"
            }

            singleChoice(PreferenceKeys.TOP_P, temperatureKeys) {
                title = "Top P"
                summary = "Controls diversity via nucleus sampling. Lower values make output more focused."
                initialSelection = "1.0"
            }

            seekBar(PreferenceKeys.TOP_K) {
                title = "Top K"
                summary = "Limits token sampling to top K most likely tokens."
                max = 60
                min = 1
                default = 40
            }

            seekBar(PreferenceKeys.MAX_TOKENS) {
                title = "Max Tokens"
                summary = "Maximum number of tokens to generate in responses."
                max = 2048
                min = 60
                default = 1024
            }
        }
    }
}
