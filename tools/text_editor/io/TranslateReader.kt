package top.fifthlight.tools.texteditor.io

import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import top.fifthlight.tools.texteditor.state.TranslateEntry
import top.fifthlight.tools.texteditor.state.TranslateState
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.useLines

object TranslateReader {
    @OptIn(ExperimentalSerializationApi::class)
    private val lineFormat = Json {
        allowTrailingComma = true
    }

    private sealed class BuildingTranslateEntry {
        abstract fun toTranslateEntry(): TranslateEntry

        data class Text(
            val key: String,
            val texts: MutableMap<String, String>,
        ) : BuildingTranslateEntry() {
            override fun toTranslateEntry() = TranslateEntry.Text(
                key = key,
                texts = texts.toPersistentMap(),
            )
        }

        data object Spacing : BuildingTranslateEntry() {
            override fun toTranslateEntry() = TranslateEntry.Spacing
        }
    }

    private fun readDefaultFile(
        file: Path,
        language: String,
        block: (Sequence<BuildingTranslateEntry>) -> Unit,
    ) {
        file.useLines { lines ->
            fun <T> Iterator<T>.nextOrNull() = if (hasNext()) {
                next()
            } else {
                null
            }

            val iterator = lines.iterator()
            val firstLine = iterator.nextOrNull() ?: error("Failed to read first line")
            check(firstLine == "{") { "First line is not {, but $firstLine" }
            block(sequence {
                while (true) {
                    when (val line = iterator.nextOrNull()?.trim()) {
                        "}" -> {
                            while (true) {
                                val line = iterator.nextOrNull() ?: break
                                check(line.isBlank()) { "Trailing content $line after }" }
                            }
                            break
                        }

                        "" -> yield(BuildingTranslateEntry.Spacing)

                        else -> {
                            val jsonString = "{$line}"
                            val jsonElement = lineFormat.decodeFromString<JsonElement>(jsonString)
                            val jsonObject = (jsonElement as? JsonObject)
                                ?.takeIf { it.size == 1 }
                                ?: error("Line $line is not a valid JSON entry")
                            val (key, valueElement) = jsonObject.entries.first()
                            val value = (valueElement as? JsonPrimitive)
                                ?.takeIf { it.isString }
                                ?.content
                                ?: error("Value $valueElement is not a valid string")
                            yield(
                                BuildingTranslateEntry.Text(
                                    key = key,
                                    texts = mutableMapOf(language to value),
                                )
                            )
                        }
                    }
                }
            })
        }
    }

    private data class BuildingTranslateState(
        val entries: MutableList<BuildingTranslateEntry> = mutableListOf(),
        val entriesMap: MutableMap<String, BuildingTranslateEntry.Text> = mutableMapOf(),
        val languages: MutableList<String> = mutableListOf(),
    ) {
        fun toTranslateState() = TranslateState(
            entries = entries.map(BuildingTranslateEntry::toTranslateEntry).toPersistentList(),
            languages = languages.toPersistentList(),
            chosenLanguage = languages.first(),
        )
    }

    fun read(directory: Path): TranslateState {
        val files = directory.listDirectoryEntries("*.json").map {
            it.fileName.nameWithoutExtension to it
        }
        val (defaultLanguage, defaultPath) = files
            .first { (language, _) -> language.equals("en_us", ignoreCase = true) }
        val buildingState = BuildingTranslateState(
            languages = mutableListOf(defaultLanguage)
        )
        readDefaultFile(defaultPath, defaultLanguage) { sequence ->
            for (entry in sequence) {
                when (val entry = entry) {
                    BuildingTranslateEntry.Spacing -> buildingState.entries += entry
                    is BuildingTranslateEntry.Text -> entry.let {
                        buildingState.entriesMap += it.key to it
                        buildingState.entries += it
                    }
                }
            }
        }
        files
            .filterNot { (_, path) -> path == defaultPath }
            .forEach { (language, path) ->
                buildingState.languages.add(language)
                val jsonObject = Json.decodeFromString<JsonObject>(path.readText())
                for ((key, valueElement) in jsonObject) {
                    val value = (valueElement as? JsonPrimitive)
                        ?.takeIf { it.isString }
                        ?.content
                        ?: error("Value $valueElement is not a valid string")
                    val entry = buildingState.entriesMap[key] ?: continue
                    entry.texts[language] = value
                }
            }
        return buildingState.toTranslateState()
    }
}
