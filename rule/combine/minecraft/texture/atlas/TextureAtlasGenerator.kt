package top.fifthlight.combine.resources.altas

import kotlinx.serialization.json.Json
import top.fifthlight.combine.resources.Metadata
import top.fifthlight.combine.resources.NinePatch
import top.fifthlight.combine.resources.NinePatchMetadata
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.jar.JarEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.math.max
import kotlin.system.exitProcess

private data class Texture(
    val identifier: String,
    val ninePatch: NinePatch?,
    val image: BufferedImage,
) {
    val size: IntSize
        get() = IntSize(
            image.width,
            image.height,
        )

    fun place(position: IntOffset) = PlacedTexture(
        identifier = identifier,
        position = position,
        size = size,
        ninePatch = ninePatch,
    )
}

fun main(vararg args: String) {
    if (args.size < 4) {
        System.err.println("Usage: TextureAtlasGenerator <namespace> <prefix> <output_jar> <output_metadata> --width <width> --height <height> [--texture <identifier> <png file> <manifest json>] [--ninepatch <identifier> <png file> <manifest json>]...")
        exitProcess(1)
    }

    val namespace = args[0]
    val prefix = args[1]
    val outputJar = Path.of(args[2])
    val outputMetadata = Path.of(args[3])

    var atlasWidth = 512
    var atlasHeight = 512
    val placedTextures = hashMapOf<String, PlacedTexture>()

    ZipOutputStream(outputJar.outputStream()).use { out ->
        fun entry(name: String) = JarEntry(name).apply {
            time = 0L
            lastAccessTime = FileTime.fromMillis(0L)
        }

        val textures = mutableListOf<Texture>()

        var i = 4
        while (i < args.size) {
            if (args.size - i < 3) {
                System.err.println("Bad texture entry")
                exitProcess(1)
            }

            val identifier = args[i + 1]
            val pngFile = Path.of(args[i + 2])
            val manifestFile = Path.of(args[i + 3])

            when (val type = args[i]) {
                "--width" -> {
                    atlasWidth = args[i + 1].toInt()
                    i += 2
                }

                "--height" -> {
                    atlasHeight = args[i + 1].toInt()
                    i += 2
                }

                "--texture" -> {
                    val manifest = Json.decodeFromString<Metadata>(manifestFile.readText())
                    if (!manifest.background) {
                        val image = ImageIO.read(pngFile.toFile())
                        textures += Texture(
                            identifier = identifier,
                            ninePatch = null,
                            image = image,
                        )
                    } else {
                        out.putNextEntry(entry("assets/$namespace/textures/gui/background/$prefix/$identifier.png"))
                        pngFile.inputStream().use { it.transferTo(out) }
                        out.closeEntry()
                    }
                    i += 4
                }

                "--ninepatch" -> {
                    val manifest = Json.decodeFromString<NinePatchMetadata>(manifestFile.readText())
                    val image = ImageIO.read(pngFile.toFile())
                    textures += Texture(
                        identifier = identifier,
                        ninePatch = manifest.ninePatch,
                        image = image,
                    )
                    i += 4
                }

                else -> {
                    System.err.println("Bad entry: $type")
                    exitProcess(1)
                }
            }
        }

        textures.sortByDescending { texture ->
            texture.size.width * texture.size.height
        }

        val outputImage = BufferedImage(atlasWidth, atlasHeight, TYPE_INT_ARGB)
        val outputGraphics = outputImage.createGraphics()
        var cursorPosition = IntOffset(0, 0)
        var maxLineHeight = 0
        for (texture in textures) {
            if (texture.size.width > atlasWidth) {
                error("Texture ${texture.identifier} too big: ${texture.size}")
            }
            if (texture.size.height + cursorPosition.y > atlasHeight) {
                error("No space left for texture ${texture.identifier}")
            }
            if (cursorPosition.x + texture.size.width > atlasWidth) {
                if (maxLineHeight == 0) {
                    error("Texture ${texture.identifier} too big: ${texture.size}")
                }
                cursorPosition = IntOffset(0, cursorPosition.y + maxLineHeight)
                maxLineHeight = 0
            }
            maxLineHeight = max(maxLineHeight, texture.size.height)
            placedTextures[texture.identifier] = texture.place(cursorPosition)
            outputGraphics.drawImage(texture.image, cursorPosition.x, cursorPosition.y, null)
            cursorPosition = IntOffset(cursorPosition.x + texture.size.width, cursorPosition.y)
        }
        outputGraphics.dispose()

        out.putNextEntry(entry("assets/$namespace/textures/gui/$prefix/atlas.png"))
        ImageIO.write(outputImage, "png", out)
        out.closeEntry()
    }

    outputMetadata.writeText(
        Json.encodeToString(
            AtlasMetadata(
                width = atlasWidth,
                height = atlasHeight,
                textures = placedTextures,
            )
        )
    )
}
