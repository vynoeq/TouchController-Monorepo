package top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component

import com.mojang.blaze3d.vertex.VertexConsumer
import org.joml.Matrix4f

fun VertexConsumer.drawBox(matrix: Matrix4f, halfSize: Float, color: Int) {
    addVertex(matrix, -halfSize, -halfSize, halfSize).setColor(color)
    addVertex(matrix, halfSize, -halfSize, halfSize).setColor(color)
    addVertex(matrix, halfSize, halfSize, halfSize).setColor(color)
    addVertex(matrix, -halfSize, halfSize, halfSize).setColor(color)

    addVertex(matrix, -halfSize, -halfSize, -halfSize).setColor(color)
    addVertex(matrix, -halfSize, halfSize, -halfSize).setColor(color)
    addVertex(matrix, halfSize, halfSize, -halfSize).setColor(color)
    addVertex(matrix, halfSize, -halfSize, -halfSize).setColor(color)

    addVertex(matrix, -halfSize, -halfSize, -halfSize).setColor(color)
    addVertex(matrix, -halfSize, -halfSize, halfSize).setColor(color)
    addVertex(matrix, -halfSize, halfSize, halfSize).setColor(color)
    addVertex(matrix, -halfSize, halfSize, -halfSize).setColor(color)

    addVertex(matrix, halfSize, -halfSize, -halfSize).setColor(color)
    addVertex(matrix, halfSize, halfSize, -halfSize).setColor(color)
    addVertex(matrix, halfSize, halfSize, halfSize).setColor(color)
    addVertex(matrix, halfSize, -halfSize, halfSize).setColor(color)

    addVertex(matrix, -halfSize, halfSize, -halfSize).setColor(color)
    addVertex(matrix, -halfSize, halfSize, halfSize).setColor(color)
    addVertex(matrix, halfSize, halfSize, halfSize).setColor(color)
    addVertex(matrix, halfSize, halfSize, -halfSize).setColor(color)

    addVertex(matrix, -halfSize, -halfSize, -halfSize).setColor(color)
    addVertex(matrix, halfSize, -halfSize, -halfSize).setColor(color)
    addVertex(matrix, halfSize, -halfSize, halfSize).setColor(color)
    addVertex(matrix, -halfSize, -halfSize, halfSize).setColor(color)
}