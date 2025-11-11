/*
 * This file is part of Cosmic IDE.
 * Cosmic IDE is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Cosmic IDE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Cosmic IDE. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cosmicide.editor

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.google.android.material.color.MaterialColors
import com.google.common.collect.ImmutableSet
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorDiagnosticTooltipWindow
import org.cosmicide.common.Prefs
import org.cosmicide.editor.language.TsLanguageJava
import org.cosmicide.extension.setCompletionLayout
import org.cosmicide.extension.setFont

class IdeEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : CodeEditor(context, attrs, defStyleAttr, defStyleRes) {

    private val ignoredPairEnds: Set<Char> = ImmutableSet.of(
        ')', ']', '}', '"', '>', '\'', ';'
    )

    val inlineCopilot = InlineCopilot(this)

    init {
        colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
        setCompletionLayout()
        setTooltipImprovements()
        setFont()
        inputType = createInputFlags()
        updateNonPrintablePaintingFlags()
        updateTextSize()
        updateTabSize()
        setInterceptParentHorizontalScrollIfNeeded(true)
        isLigatureEnabled = Prefs.useLigatures
        isWordwrap = Prefs.wordWrap
        setScrollBarEnabled(Prefs.scrollbarEnabled)
        isHardwareAcceleratedDrawAllowed = Prefs.hardwareAcceleration
        isLineNumberEnabled = Prefs.lineNumbers
        props.deleteEmptyLineFast = Prefs.quickDelete
        props.stickyScroll = Prefs.stickyScroll
        
        // Subscribe to text changes for inline copilot
        subscribeEvent(ContentChangeEvent::class.java) { event, _ ->
            if (Prefs.copilotEnabled) {
                inlineCopilot.requestCompletion()
            }
        }
    }

    override fun commitText(text: CharSequence?, applyAutoIndent: Boolean) {
        if (text?.length == 1) {
            val currentChar = text.toString().getOrNull(cursor.left)
            val c = text[0]
            if (ignoredPairEnds.contains(c) && c == currentChar) {
                setSelection(cursor.leftLine, cursor.leftColumn + 1)
                return
            }
        }
        super.commitText(text, applyAutoIndent)
    }

    fun appendText(text: String): Int {
        val content = getText()
        if (lineCount <= 0) {
            return 0
        }
        var col = content.getColumnCount(lineCount - 1)
        if (col < 0) {
            col = 0
        }
        content.insert(lineCount - 1, col, text)
        return lineCount - 1
    }

    private fun updateTextSize() {
        setTextSize(Prefs.editorFontSize)
    }

    private fun updateTabSize() {
        tabWidth = Prefs.tabSize
    }

    private fun updateNonPrintablePaintingFlags() {
        val flags = (FLAG_DRAW_WHITESPACE_LEADING
                or FLAG_DRAW_WHITESPACE_INNER
                or FLAG_DRAW_WHITESPACE_FOR_EMPTY_LINE)
        nonPrintablePaintingFlags = if (Prefs.nonPrintableCharacters) flags else 0
    }

    private fun createInputFlags(): Int {
        return EditorInfo.TYPE_CLASS_TEXT or
                EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE or
                EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    }

    private fun setTooltipImprovements() {
        getComponent(EditorDiagnosticTooltipWindow::class.java).apply {
            setSize(500, 100)
            parentView.setBackgroundColor(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorErrorContainer,
                    null
                )
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if (editorLanguage is TsLanguageJava) {
            (editorLanguage as TsLanguageJava).onConfigurationChanged()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inlineCopilot.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Accept copilot suggestion with Tab key
        if (keyCode == KeyEvent.KEYCODE_TAB && Prefs.copilotEnabled) {
            inlineCopilot.acceptSuggestion()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
