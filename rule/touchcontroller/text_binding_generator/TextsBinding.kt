package top.fifthlight.touchcontroller.resources.generator

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

fun main(vararg args: String) {
    val languageFile = Path.of(args[0])
    val outputFile = Path.of(args[1])
    val className = args[2]
    val packageName = args[3]
    val map: Map<String, String> = Json.decodeFromString(languageFile.readText())

    val textsBuilder = TypeSpec.objectBuilder("Texts")

    for ((key, value) in map) {
        if (!key.startsWith("touchcontroller.")) {
            System.err.println("Key $key don't start with touchcontroller, skip it.")
            continue
        }
        val strippedKey = key.removePrefix("touchcontroller.")
        val transformedKey = strippedKey.uppercase().replace('.', '_')

        textsBuilder.addProperty(
            PropertySpec
                .builder(transformedKey, ClassName("top.fifthlight.combine.data", "Identifier"))
                .addKdoc("Translation text: %L", value)
                .initializer("Identifier.Namespaced(%S, %S)", "touchcontroller", strippedKey)
                .build()
        )
    }

    val texts = textsBuilder.build()
    val file = FileSpec
        .builder(packageName, className)
        .addAnnotation(
            AnnotationSpec
                .builder(Suppress::class)
                .addMember("%S", "RedundantVisibilityModifier")
                .build()
        )
        .addType(texts)
        .build()
    buildString {
        file.writeTo(this)
    }.let {
        outputFile.writeText(it)
    }
}
