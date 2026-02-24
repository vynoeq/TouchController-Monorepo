package top.fifthlight.touchcontroller.version_26_1.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.backend.minecraft_26_1.CanvasImpl
import top.fifthlight.combine.backend.minecraft_26_1.toMinecraft
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.layout.measure.MeasurePolicy
import top.fifthlight.combine.layout.measure.fixed
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.widget.Canvas
import top.fifthlight.data.IntSize
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.ui.widget.raw.RawTextureIcon

@ActualImpl(RawTextureIcon::class)
object RawTextureIconImpl : RawTextureIcon {
    @JvmStatic
    @ActualConstructor
    fun of(): RawTextureIcon = RawTextureIconImpl

    @Composable
    override fun Icon(
        modifier: Modifier,
        identifier: Identifier,
        size: IntSize,
    ) = Canvas(
        modifier = modifier,
        measurePolicy = MeasurePolicy.fixed(size),
    ) { canvas, node ->
        val guiGraphics = (canvas as CanvasImpl).guiGraphics
        guiGraphics.blit(identifier.toMinecraft(), 0, 0, node.width, node.height, 0f, 1f, 0f, 1f)
    }
}
