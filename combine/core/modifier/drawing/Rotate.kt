package top.fifthlight.combine.modifier.drawing

import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.data.Offset

fun Modifier.rotate(degrees: Float) = then(RotateModifierNode(degrees))

private data class RotateModifierNode(
    val degrees: Float
) : DrawModifierNode, Modifier.Node<RotateModifierNode> {
    override fun Canvas.renderBefore(wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {
        pushState()
        translate(wrapperNode.width / 2, wrapperNode.height / 2)
        rotate(degrees)
        translate(-wrapperNode.width / 2, -wrapperNode.height / 2)
    }

    override fun Canvas.renderAfter(wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {
        popState()
    }
}
