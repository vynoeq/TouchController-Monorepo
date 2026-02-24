package top.fifthlight.blazerod.render.version_1_21_8.expect

import com.mojang.blaze3d.vertex.VertexFormatElement
import top.fifthlight.mergetools.api.ExpectFactory

@Suppress("PropertyName")
interface IrisApis {
    val ENTITY_ID_ELEMENT: VertexFormatElement
    val MID_TEXTURE_ELEMENT: VertexFormatElement
    val TANGENT_ELEMENT: VertexFormatElement
    val shaderPackInUse: Boolean

    @ExpectFactory
    interface Factory {
        fun create(): IrisApis
    }

    companion object : IrisApis by IrisApisFactory.create()
}
