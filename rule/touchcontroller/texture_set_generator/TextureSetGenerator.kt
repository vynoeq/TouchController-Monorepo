package top.fifthlight.touchcontroller.resources.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

private data class TextureInput(
    val name: String,
    val identifier: String,
)

private data class TextureSetInput(
    val id: String,
    val metadataPath: Path,
    val textures: List<TextureInput>,
)

@Serializable
private data class TextureSetMetadata(
    @SerialName("gray_when_active")
    val grayWhenActive: Boolean = false,
    @SerialName("base")
    val base: String? = null,
    @SerialName("fallback")
    val fallback: Map<String, String> = mapOf(),
)

private data class TextureSetItem(
    val metadata: TextureSetMetadata,
    val textures: Map<String, String>,
)

private fun String.snakeToCamelCase(firstCharUppercase: Boolean = false) = this.split('_')
    .joinToString("") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    .replaceFirstChar {
        if (firstCharUppercase) {
            it.uppercaseChar()
        } else {
            it.lowercaseChar()
        }
    }

private fun parseTextureSets(textureSets: List<TextureSetInput>) =
    textureSets.associate { set ->
        set.id to TextureSetItem(
            metadata = Json.decodeFromString<TextureSetMetadata>(set.metadataPath.readText()),
            textures = set.textures.associate { it.name to it.identifier }
        )
    }

private fun generateTextureSet(
    packageName: String,
    className: String,
    textPackage: String,
    textClass: String,
    texturePackage: String,
    textureClass: String,
    metadataMap: Map<String, TextureSetItem>,
): FileSpec {
    val textClassName = ClassName(textPackage, textClass)
    val textureClassName = ClassName(texturePackage, textureClass)
    val textureSetClassName = ClassName(packageName, className)
    val textureTypeName = ClassName("top.fifthlight.combine.paint", "Texture")
    val identifierTypeName = ClassName("top.fifthlight.combine.data", "Identifier")

    val allTextures = metadataMap.values
        .flatMap { it.textures.keys }
        .distinct()
        .sorted()

    val textureSetBuilder = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.SEALED)

    textureSetBuilder.addType(
        TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("textures", textureClassName)
                    .initializer("%T.of()", ClassName(texturePackage, textureClass + "Factory"))
                    .build()
            )
            .build()
    )

    for (texture in allTextures) {
        val propertyName = texture.snakeToCamelCase()
        PropertySpec.builder(propertyName, textureTypeName).apply {
            if (propertyName.endsWith("Active")) {
                addModifiers(KModifier.OPEN)
                getter(
                    FunSpec.getterBuilder()
                        .addCode("return %L", propertyName.removeSuffix("Active"))
                        .build()
                )
            } else {
                addModifiers(KModifier.ABSTRACT)
            }
        }.build().let(textureSetBuilder::addProperty)
    }

    for ((setId, setItem) in metadataMap.entries.sortedBy { it.key }) {
        val (metadata, textures) = setItem
        val classTypeName = setId.snakeToCamelCase(true)

        val superclass = metadata.base?.let { baseId ->
            ClassName(packageName, className, baseId.snakeToCamelCase(true))
        } ?: textureSetClassName

        val subclassBuilder = TypeSpec.classBuilder(classTypeName)
            .addModifiers(KModifier.OPEN)
            .superclass(superclass)

        for ((name, identifier) in textures) {
            val propertyName = name.snakeToCamelCase()
            PropertySpec.builder(propertyName, textureTypeName)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("textures.%L", identifier)
                .build()
                .let(subclassBuilder::addProperty)
        }

        for ((target, source) in metadata.fallback) {
            val propertyName = target.snakeToCamelCase()
            PropertySpec.builder(propertyName, textureTypeName)
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addCode("return %L", source.snakeToCamelCase())
                        .build()
                )
                .build()
                .let(subclassBuilder::addProperty)
        }

        subclassBuilder.addType(
            TypeSpec.companionObjectBuilder()
                .addProperty(
                    PropertySpec.builder(
                        "INSTANCE",
                        ClassName(packageName, className, classTypeName)
                    ).delegate("lazy { %L() }", classTypeName)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder(
                        "key",
                        ClassName(packageName, className, "TextureSetKey")
                    ).getter(
                        FunSpec.getterBuilder()
                            .addCode("return TextureSetKey.%L", setId.uppercase())
                            .build()
                    )
                        .build()
                )
                .build()
        )

        textureSetBuilder.addType(subclassBuilder.build())
    }

    val textureKeyBuilder = TypeSpec.classBuilder("TextureKey")
        .addModifiers(KModifier.SEALED)
        .addAnnotation(Serializable::class)
        .addProperty(
            PropertySpec.builder("name", String::class)
                .addModifiers(KModifier.ABSTRACT)
                .build()
        )

    val getLambdaType = LambdaTypeName.get(
        parameters = arrayOf(ClassName(packageName, className)),
        returnType = textureTypeName,
    )
    textureKeyBuilder.addProperty(
        PropertySpec.builder("get", getLambdaType)
            .addModifiers(KModifier.ABSTRACT)
            .build()
    )

    for (texture in allTextures) {
        val objectName = texture.snakeToCamelCase(true)
        textureKeyBuilder.addType(
            TypeSpec.objectBuilder(objectName)
                .superclass(ClassName(packageName, className, "TextureKey"))
                .addModifiers(KModifier.DATA)
                .addAnnotation(Serializable::class)
                .addAnnotation(
                    AnnotationSpec.builder(SerialName::class)
                        .addMember("%S", texture)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("name", String::class)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%S", texture.snakeToCamelCase(true))
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("get", getLambdaType)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("%T::%L", ClassName(packageName, className), texture.snakeToCamelCase())
                        .build()
                )
                .build()
        )
    }

    textureKeyBuilder.addType(
        TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder(
                    "all",
                    List::class.asClassName().parameterizedBy(ClassName(packageName, className, "TextureKey"))
                ).delegate("lazy { listOf(%L) }", allTextures.joinToString(",\n") {
                    it.snakeToCamelCase(true)
                }).build()
            )
            .build()
    )

    textureSetBuilder.addType(textureKeyBuilder.build())

    val textureSetKeyBuilder = TypeSpec.enumBuilder("TextureSetKey")
        .addAnnotation(Serializable::class)

    textureSetKeyBuilder.primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter(ParameterSpec.builder("nameText", identifierTypeName).build())
            .addParameter(ParameterSpec.builder("titleText", identifierTypeName).build())
            .addParameter(ParameterSpec.builder("textureSet", ClassName(packageName, className)).build())
            .build()
    )

    textureSetKeyBuilder.addProperty(
        PropertySpec.builder("nameText", identifierTypeName)
            .initializer("nameText")
            .build()
    )

    textureSetKeyBuilder.addProperty(
        PropertySpec.builder("titleText", identifierTypeName)
            .initializer("titleText")
            .build()
    )

    textureSetKeyBuilder.addProperty(
        PropertySpec.builder("textureSet", ClassName(packageName, className))
            .initializer("textureSet")
            .build()
    )

    for ((setId, _) in metadataMap.entries.sortedBy { it.key }) {
        val classTypeName = setId.snakeToCamelCase(true)
        textureSetKeyBuilder.addEnumConstant(
            setId.uppercase(),
            TypeSpec.anonymousClassBuilder()
                .addAnnotation(
                    AnnotationSpec.builder(SerialName::class)
                        .addMember("%S", setId)
                        .build()
                )
                .addSuperclassConstructorParameter("%T.TEXTURE_SET_%L_NAME", textClassName, setId.uppercase())
                .addSuperclassConstructorParameter("%T.TEXTURE_SET_%L_TITLE", textClassName, setId.uppercase())
                .addSuperclassConstructorParameter("%L.INSTANCE", classTypeName)
                .build()
        )
    }

    textureSetBuilder.addType(textureSetKeyBuilder.build())

    return FileSpec.builder(packageName, className)
        .addAnnotation(
            AnnotationSpec.builder(Suppress::class)
                .addMember("%S", "RedundantVisibilityModifier")
                .build()
        )
        .addType(textureSetBuilder.build())
        .build()
}

private fun run(
    output: Path,
    packageName: String,
    className: String,
    textPackage: String,
    textClass: String,
    texturePackage: String,
    textureClass: String,
    textureSets: List<TextureSetInput>,
) {
    val metadataMap = parseTextureSets(textureSets)
    val fileSpec = generateTextureSet(
        packageName = packageName,
        className = className,
        textPackage = textPackage,
        textClass = textClass,
        texturePackage = texturePackage,
        textureClass = textureClass,
        metadataMap = metadataMap,
    )
    output.writeText(buildString { fileSpec.writeTo(this) })
}

fun main(vararg args: String) {
    var output: Path? = null
    var packageName: String? = null
    var className: String? = null
    var texturePackage: String? = null
    var textureClass: String? = null
    var textPackage: String? = null
    var textClass: String? = null
    val textureSets = mutableListOf<TextureSetInput>()

    var currentSet: Pair<String, Path>? = null
    val currentTextures = mutableListOf<TextureInput>()

    fun flushCurrentSet() {
        val set = currentSet ?: return
        textureSets.add(
            TextureSetInput(
                set.first,
                set.second,
                currentTextures.toList()
            )
        )
        currentTextures.clear()
        currentSet = null
    }

    var i = 0

    fun nextArg() = args[i++]
    while (i in args.indices) {
        when (val arg = nextArg()) {
            "--output" -> output = Path.of(nextArg())
            "--package" -> packageName = nextArg()
            "--class_name" -> className = nextArg()
            "--texture_package" -> texturePackage = nextArg()
            "--texture_class" -> textureClass = nextArg()
            "--text_package" -> textPackage = nextArg()
            "--text_class" -> textClass = nextArg()
            "--set" -> {
                flushCurrentSet()
                currentSet = Pair(nextArg(), Path.of(nextArg()))
            }

            "--texture" -> {
                val (name, identifier) = nextArg().split(':', limit = 2)
                currentTextures.add(TextureInput(name, identifier))
            }

            else -> throw IllegalArgumentException("Bad argument: $arg")
        }
    }

    flushCurrentSet()
    run(
        output = requireNotNull(output) { "No output" },
        packageName = requireNotNull(packageName) { "No package name" },
        className = requireNotNull(className) { "No class name" },
        textPackage = requireNotNull(textPackage) { "No text package name" },
        textClass = requireNotNull(textClass) { "No text class" },
        texturePackage = requireNotNull(texturePackage) { "No texture package name" },
        textureClass = requireNotNull(textureClass) { "No texture class" },
        textureSets = textureSets,
    )
}
