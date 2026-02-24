package top.fifthlight.tools.texteditor.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import top.fifthlight.tools.texteditor.io.TranslateReader
import top.fifthlight.tools.texteditor.io.TranslateWriter
import top.fifthlight.tools.texteditor.state.TranslateEntry
import top.fifthlight.tools.texteditor.state.TranslateState
import java.nio.file.Path
import kotlin.io.path.pathString

class TranslateViewModel {
    private val _state = MutableStateFlow(TranslateState())
    val state: StateFlow<TranslateState> = _state.asStateFlow()

    private var currentDirectory: Path? = null

    fun loadDirectory(path: Path) {
        val translateState = TranslateReader.read(path)
        currentDirectory = path
        _state.value = translateState.copy(
            currentDirectory = path.pathString,
        )
    }

    fun saveDirectory(path: Path? = null) {
        val targetPath = path ?: currentDirectory ?: return
        TranslateWriter.write(targetPath, _state.value)
        _state.update { it.copy(hasUnsavedChanges = false) }
    }

    fun insertEntry(index: Int, entry: TranslateEntry) {
        _state.update { state ->
            state.copy(
                entries = state.entries.add(index.coerceIn(0, state.entries.size), entry),
                hasUnsavedChanges = true,
            )
        }
    }

    fun updateText(index: Int, language: String, text: String) {
        _state.update { state ->
            val entries = state.entries
            if (index !in entries.indices) return@update state

            val entry = entries[index]
            if (entry !is TranslateEntry.Text) return@update state

            state.copy(
                entries = entries.set(index, entry.copy(texts = entry.texts.put(language, text))),
                hasUnsavedChanges = true
            )
        }
    }

    fun updateKey(index: Int, newKey: String) {
        _state.update { state ->
            val entries = state.entries
            if (index !in entries.indices) return@update state

            val entry = entries[index]
            if (entry !is TranslateEntry.Text) return@update state

            state.copy(
                entries = entries.set(index, entry.copy(key = newKey)),
                hasUnsavedChanges = true
            )
        }
    }

    fun deleteEntry(index: Int) {
        _state.update { state ->
            val entries = state.entries
            if (index !in entries.indices) return@update state

            state.copy(
                entries = entries.removeAt(index),
                hasUnsavedChanges = true
            )
        }
    }

    fun addLanguage(language: String) {
        _state.update { state ->
            if (language in state.languages) return@update state
            state.copy(
                languages = state.languages.add(language),
                hasUnsavedChanges = true
            )
        }
    }

    fun chooseLanguage(language: String) {
        _state.update {
            it.copy(chosenLanguage = language)
        }
    }
}
