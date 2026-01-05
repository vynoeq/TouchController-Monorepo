package top.fifthlight.combine.resources.vanilla

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import top.fifthlight.combine.resources.Metadata
import top.fifthlight.combine.resources.NinePatchMetadata
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.jar.JarEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.system.exitProcess

@Serializable
data class TextureMetadata(val gui: Gui) {
    @Serializable
    data class Gui(val scaling: Scaling) {
        @Serializable
        sealed interface Scaling {
            @Serializable
            @SerialName("nine_slice")
            data class NineSlice(
                val width: Int,
                val height: Int,
                val border: Border,
                @SerialName("stretch_inner")
                val stretchInner: Boolean,
            ): Scaling {
                @Serializable
                data class Border(
                    val left: Int,
                    val top: Int,
                    val right: Int,
                    val bottom: Int,
                )
            }

            @Serializable
            @SerialName("tile")
            data class Tile(
                val width: Int,
                val height: Int,
            ): Scaling
        }
    }
}

fun main(vararg args: String) {
    if (args.size < 3) {
        System.err.println("Usage: VanillaTextureGenerator <namespace> <prefix> <output_jar> [--texture <identifier> <png file> <manifest json>] [--ninepatch <identifier> <png file> <manifest json>]...")
        exitProcess(1)
    }

    val namespace = args[0]
    val prefix = args[1]
    val outputJar = Path.of(args[2])

    ZipOutputStream(outputJar.outputStream()).use { out ->
        fun entry(name: String) = JarEntry(name).apply {
            time = 0L
            lastAccessTime = FileTime.fromMillis(0L)
        }

        var i = 3
        while (i < args.size) {
            if (args.size - i < 3) {
                System.err.println("Bad texture entry")
                exitProcess(1)
            }

            val identifier = args[i + 1]
            val pngFile = Path.of(args[i + 2])
            val manifestFile = Path.of(args[i + 3])

            when (val type = args[i]) {
                "--texture" -> {
                    val manifest = Json.decodeFromString<Metadata>(manifestFile.readText())
                    if (!manifest.background) {
                        out.putNextEntry(entry("assets/$namespace/textures/gui/sprites/$prefix/$identifier.png"))
                        pngFile.inputStream().use { it.transferTo(out) }
                        out.closeEntry()
                    } else {
                        out.putNextEntry(entry("assets/$namespace/textures/gui/sprites/$prefix/$identifier.png"))
                        pngFile.inputStream().use { it.transferTo(out) }
                        out.closeEntry()

                        val image = ImageIO.read(pngFile.toFile())
                        val meta = TextureMetadata(
                            gui = TextureMetadata.Gui(
                                scaling = TextureMetadata.Gui.Scaling.Tile(
                                    width = image.width,
                                    height = image.height,
                                ),
                            )
                        )
                        out.putNextEntry(entry("assets/$namespace/textures/gui/sprites/$prefix/$identifier.png.mcmeta"))
                        out.write(Json.encodeToString(TextureMetadata.serializer(), meta).toByteArray())
                        out.closeEntry()
                    }
                    i += 4
                }

                "--ninepatch" -> {
                    val manifest = Json.decodeFromString<NinePatchMetadata>(manifestFile.readText())
                    out.putNextEntry(entry("assets/$namespace/textures/gui/sprites/$prefix/$identifier.png"))
                    pngFile.inputStream().use { it.transferTo(out) }
                    out.closeEntry()

                    val image = ImageIO.read(pngFile.toFile())
                    val meta = TextureMetadata(
                        gui = TextureMetadata.Gui(
                            scaling = TextureMetadata.Gui.Scaling.NineSlice(
                                width = image.width,
                                height = image.height,
                                border = TextureMetadata.Gui.Scaling.NineSlice.Border(
                                    left = manifest.ninePatch.scaleArea.left,
                                    top = manifest.ninePatch.scaleArea.top,
                                    right = image.width - manifest.ninePatch.scaleArea.right,
                                    bottom = image.height - manifest.ninePatch.scaleArea.bottom,
                                ),
                                stretchInner = true,
                            ),
                        ),
                    )
                    out.putNextEntry(entry("assets/$namespace/textures/gui/sprites/$prefix/$identifier.png.mcmeta"))
                    out.write(Json.encodeToString(TextureMetadata.serializer(), meta).toByteArray())
                    out.closeEntry()
                    i += 4
                }

                else -> {
                    System.err.println("Bad entry: $type")
                    exitProcess(1)
                }
            }
        }
    }
}