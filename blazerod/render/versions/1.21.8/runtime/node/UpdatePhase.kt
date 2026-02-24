package top.fifthlight.blazerod.render.version_1_21_8.runtime.node

import net.minecraft.client.renderer.MultiBufferSource
import org.joml.Matrix4f
import org.joml.Matrix4fc
import top.fifthlight.blazerod.render.common.util.objectpool.ObjectPool

sealed class UpdatePhase(
    val type: Type,
) {
    enum class Type {
        IK_UPDATE,
        INFLUENCE_TRANSFORM_UPDATE,
        PHYSICS_UPDATE_PRE,
        PHYSICS_UPDATE_POST,
        GLOBAL_TRANSFORM_PROPAGATION,
        RENDER_DATA_UPDATE,
        CAMERA_UPDATE,
        DEBUG_RENDER,
    }

    data object IkUpdate : UpdatePhase(Type.IK_UPDATE)

    data object InfluenceTransformUpdate : UpdatePhase(Type.INFLUENCE_TRANSFORM_UPDATE)

    data object PhysicsUpdatePre : UpdatePhase(Type.PHYSICS_UPDATE_PRE)

    data object PhysicsUpdatePost : UpdatePhase(Type.PHYSICS_UPDATE_POST)


    data object GlobalTransformPropagation : UpdatePhase(Type.GLOBAL_TRANSFORM_PROPAGATION)

    data object RenderDataUpdate : UpdatePhase(Type.RENDER_DATA_UPDATE)

    data object CameraUpdate : UpdatePhase(Type.CAMERA_UPDATE)

    @ConsistentCopyVisibility
    data class DebugRender private constructor(
        val viewProjectionMatrix: Matrix4f = Matrix4f(),
        val cacheMatrix: Matrix4f = Matrix4f(),
        private var _multiBufferSource: MultiBufferSource? = null,
    ) : UpdatePhase(
        type = Type.DEBUG_RENDER,
    ), AutoCloseable {
        private var recycled = false

        val multiBufferSource: MultiBufferSource
            get() = _multiBufferSource!!

        override fun close() {
            if (recycled) {
                return
            }
            recycled = true
            POOL.release(this)
        }

        companion object {
            private val POOL = ObjectPool(
                identifier = "update_phase_debug_render",
                create = ::DebugRender,
                onAcquired = {
                    recycled = false
                },
                onReleased = { _multiBufferSource = null },
                onClosed = {},
            )

            fun acquire(
                viewProjectionMatrix: Matrix4fc,
                multiBufferSource: MultiBufferSource,
            ) = POOL.acquire().apply {
                this.viewProjectionMatrix.set(viewProjectionMatrix)
                _multiBufferSource = multiBufferSource
            }
        }
    }
}
