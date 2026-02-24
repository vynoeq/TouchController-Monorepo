package top.fifthlight.combine.resources.vanilla

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.Json
import top.fifthlight.combine.resources.Metadata
import top.fifthlight.combine.resources.NinePatchMetadata
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.system.exitProcess

fun main(vararg args: String) {
    if (args.size < 2) {
        System.err.println("Usage: KotlinTextureLibraryGenerator <output_file> <package name> <class name> <prefix> <namespace> [--texture <identifier> <png file> <manifest json>] [--ninepatch <identifier> <png file> <manifest json>]...")
        exitProcess(1)
    }

    val outputFile = Path.of(args[0])
    val packageName = args[1]
    val className = args[2]
    val prefix = args[3]
    val namespace = args[4]

    val classSpecBuilder = TypeSpec.objectBuilder(className + "Impl")
        .addSuperinterface(ClassName(packageName, className))
        .addAnnotation(
            AnnotationSpec.builder(ClassName("top.fifthlight.mergetools.api", "ActualImpl"))
                .addMember("$packageName.$className::class")
                .build()
        )
        .addFunction(
            FunSpec.builder("of")
                .addAnnotation(JvmStatic::class)
                .addAnnotation(
                    AnnotationSpec.builder(ClassName("top.fifthlight.mergetools.api", "ActualConstructor")).build()
                )
                .returns(ClassName(packageName, className))
                .addCode("return %LImpl", className)
                .build()
        )

    var i = 5
    while (i < args.size) {
        if (args.size - i < 3) {
            System.err.println("Bad texture entry")
            exitProcess(1)
        }

        val identifier = args[i + 1]
        when (val type = args[i]) {
            "--texture" -> {
                val metadata = Json.decodeFromString<Metadata>(Path.of(args[i + 3]).readText())
                if (metadata.background) {
                    classSpecBuilder.addProperty(
                        PropertySpec.builder(
                            identifier,
                            ClassName("top.fifthlight.combine.paint", "BackgroundTexture")
                        ).addModifiers(KModifier.OVERRIDE).initializer(
                            "BackgroundTextureFactory.create(%S, %S, %L, %L)",
                            namespace,
                            "textures/gui/$prefix/$identifier.png",
                            metadata.size.width,
                            metadata.size.height,
                        ).build()
                    )
                } else {
                    classSpecBuilder.addProperty(
                        PropertySpec.builder(
                            identifier,
                            ClassName("top.fifthlight.combine.paint", "Texture")
                        ).addModifiers(KModifier.OVERRIDE).initializer(
                            "TextureFactory.create(%S, %S, %L, %L, IntPadding.ZERO)",
                            namespace,
                            "$prefix/$identifier",
                            metadata.size.width,
                            metadata.size.height,
                        ).build()
                    )
                }
                i += 4
            }

            "--ninepatch" -> {
                val metadata = Json.decodeFromString<NinePatchMetadata>(Path.of(args[i + 3]).readText())
                classSpecBuilder.addProperty(
                    PropertySpec
                        .builder(identifier, ClassName("top.fifthlight.combine.paint", "Texture"))
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer(
                            "TextureFactory.create(%S, %S, %L, %L, IntPadding(%L, %L, %L, %L))",
                            namespace,
                            "$prefix/$identifier",
                            metadata.size.width,
                            metadata.size.height,
                            metadata.ninePatch.padding.left,
                            metadata.ninePatch.padding.top,
                            metadata.ninePatch.padding.right,
                            metadata.ninePatch.padding.bottom,
                        )
                        .build()
                )
                i += 4
            }

            else -> {
                System.err.println("Bad entry: $type")
                exitProcess(1)
            }
        }
    }

    val file = FileSpec
        .builder(packageName, className)
        .addAnnotation(
            AnnotationSpec
                .builder(Suppress::class)
                .addMember("%S", "RedundantVisibilityModifier")
                .build()
        )
        .addImport("top.fifthlight.combine.paint", "TextureFactory")
        .addImport("top.fifthlight.combine.paint", "BackgroundTextureFactory")
        .addImport("top.fifthlight.data", "IntPadding")
        .addType(classSpecBuilder.build())
        .build()
    outputFile.writeText(buildString { file.writeTo(this) })
}
