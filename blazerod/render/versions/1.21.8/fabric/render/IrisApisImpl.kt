package top.fifthlight.blazerod.render.version_1_21_8.fabric.render

import com.mojang.blaze3d.vertex.VertexFormatElement
import net.fabricmc.loader.api.FabricLoader
import net.irisshaders.iris.api.v0.IrisApi
import net.irisshaders.iris.vertices.IrisVertexFormats
import top.fifthlight.blazerod.render.version_1_21_8.expect.IrisApis
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(IrisApis::class)
class IrisApisImpl @ActualConstructor("create") constructor() : IrisApis {
    override val ENTITY_ID_ELEMENT: VertexFormatElement
        get() = IrisVertexFormats.ENTITY_ID_ELEMENT
    override val MID_TEXTURE_ELEMENT: VertexFormatElement
        get() = IrisVertexFormats.MID_TEXTURE_ELEMENT
    override val TANGENT_ELEMENT: VertexFormatElement
        get() = IrisVertexFormats.TANGENT_ELEMENT

    private val irisApi = if (FabricLoader.getInstance().isModLoaded("iris")) {
        IrisApi.getInstance()
    } else {
        null
    }

    override val shaderPackInUse: Boolean
        get() = irisApi?.isShaderPackInUse == true
}
