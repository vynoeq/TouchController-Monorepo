package top.fifthlight.blazerod.render.version_1_21_8.api.resource

import org.joml.Matrix4fc
import net.minecraft.client.renderer.MultiBufferSource
import top.fifthlight.blazerod.render.api.resource.ModelInstance

interface DebugRenderable {
    fun debugRender(viewProjectionMatrix: Matrix4fc, bufferSource: MultiBufferSource, time: Float)
}

fun ModelInstance.debugRender(viewProjectionMatrix: Matrix4fc, bufferSource: MultiBufferSource, time: Float) =
    (this as DebugRenderable).debugRender(viewProjectionMatrix, bufferSource, time)
