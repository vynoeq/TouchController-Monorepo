package top.fifthlight.blazerod.common.resource

import org.joml.Matrix4fc

class RenderSkin(
    val name: String?,
    val inverseBindMatrices: List<org.joml.Matrix4fc>?,
    val jointSize: Int,
)
