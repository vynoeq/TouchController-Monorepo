package top.fifthlight.tools.texteditor.io

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import top.fifthlight.tools.texteditor.state.TranslateEntry
import top.fifthlight.tools.texteditor.state.TranslateState
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writer

object TranslateWriter {
    private fun escapeString(string: String) = Json.encodeToString(string)

    fun write(directory: Path, state: TranslateState) {
        directory.createDirectories()
        for (language in state.languages) {
            val file = directory.resolve("$language.json")
            val lastValidTextIndex = state.entries.indexOfLast { entry ->
                entry is TranslateEntry.Text && entry.texts.containsKey(language)
            }
            PrintWriter(file.writer()).use { writer ->
                writer.println("{")
                for ((index, entry) in state.entries.withIndex()) {
                    when (entry) {
                        is TranslateEntry.Text -> {
                            if (!entry.texts.containsKey(language)) {
                                writer.println()
                                continue
                            }
                            val key = escapeString(entry.key)
                            val value = entry.texts[language]!!.let(::escapeString)
                            val isLast = index == lastValidTextIndex
                            writer.println("  $key: $value${if (isLast) "" else ","}")
                        }

                        is TranslateEntry.Spacing -> {
                            writer.println()
                        }
                    }
                }
                writer.println("}")
            }
        }
    }
}
