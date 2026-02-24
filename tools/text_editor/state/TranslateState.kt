package top.fifthlight.tools.texteditor.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class TranslateState(
    val entries: PersistentList<TranslateEntry> = persistentListOf(),
    val languages: PersistentList<String> = persistentListOf(),
    val hasUnsavedChanges: Boolean = false,
    val chosenLanguage: String? = null,
    val currentDirectory: String? = null,
)
