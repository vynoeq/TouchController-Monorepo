package top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component

import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import top.fifthlight.blazerod.render.common.util.refcount.AbstractRefCount
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.RenderNodeImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.UpdatePhase
import top.fifthlight.blazerod.render.version_1_21_8.runtime.ModelInstanceImpl
import java.util.*

sealed class RenderNodeComponent<C : RenderNodeComponent<C>> : AbstractRefCount() {
    companion object {
        protected val DEBUG_RENDER_LAYER: RenderType.CompositeRenderType = RenderType.create(
            "blazerod_joint_debug_lines",
            1536,
            RenderPipelines.LINES,
            RenderType.CompositeState.builder()
                .setLineState(RenderStateShard.LineStateShard(OptionalDouble.of(1.0)))
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .createCompositeState(false)
        )
    }

    override val typeId: String
        get() = "node"

    sealed class Type<C : RenderNodeComponent<C>> {
        object Primitive : Type<PrimitiveComponent>()
        object Joint : Type<JointComponent>()
        object InfluenceSource : Type<InfluenceSourceComponent>()
        object Camera : Type<CameraComponent>()
        object IkTarget : Type<IkTargetComponent>()
        object RigidBody : Type<RigidBodyComponent>()
    }

    abstract val type: Type<C>

    open fun onAttached(instance: ModelInstanceImpl, node: RenderNodeImpl) {}

    abstract val updatePhases: List<UpdatePhase.Type>
    abstract fun update(phase: UpdatePhase, node: RenderNodeImpl, instance: ModelInstanceImpl)

    lateinit var node: RenderNodeImpl
        internal set
}

