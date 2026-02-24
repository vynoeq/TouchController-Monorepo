package top.fifthlight.combine.modifier.drawing

import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.combine.paint.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.Offset

fun Modifier.background(drawable: Drawable) = then(DrawableBackgroundNode(drawable))

fun Modifier.background(color: Color) = background(ColorDrawable(color))

fun Modifier.background(texture: BackgroundTexture, scale: Float = 1f) =
    background(BackgroundTextureDrawable(texture, scale))

private data class DrawableBackgroundNode(
    val drawable: Drawable
) : DrawModifierNode, Modifier.Node<DrawableBackgroundNode> {
    override fun renderBefore(canvas: Canvas, wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {
        drawable.draw(canvas, IntRect(offset = IntOffset.ZERO, size = wrapperNode.size))
    }
}
