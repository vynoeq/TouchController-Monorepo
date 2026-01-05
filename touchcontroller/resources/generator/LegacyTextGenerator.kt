package top.fifthlight.touchcontroller.resources.generator

import kotlinx.serialization.json.Json
import java.io.StringWriter
import java.nio.file.Path
import java.util.*
import kotlin.io.path.readText
import kotlin.io.path.writer

fun main(vararg args: String) {
    val file = Path.of(args[0])
    val outputFile = Path.of(args[1])
    val map: Map<String, String> = Json.decodeFromString(file.readText())
    val writeBuffer = StringWriter()
    Properties().apply {
        map.entries.forEach { (key, value) ->
            put(key, value.replace("%d", "%s"))
        }
    }.store(writeBuffer, "PARSE_ESCAPES")
    outputFile.writer().use { writer ->
        writeBuffer
            .toString()
            .lineSequence()
            .filterIndexed { index, _ -> index != 1 }
            .forEach {
                writer.write(it)
                writer.write('\n'.code)
            }
    }
}
