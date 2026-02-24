package top.fifthlight.tools.texteditor.state

import kotlinx.collections.immutable.PersistentMap

sealed class TranslateEntry {
    data class Text(
        val key: String,
        val texts: PersistentMap<String, String>,
    ) : TranslateEntry()

    data object Spacing : TranslateEntry()
}
