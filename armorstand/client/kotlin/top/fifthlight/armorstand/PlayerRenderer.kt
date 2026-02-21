package top.fifthlight.armorstand

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.PlayerRenderState
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.state.ModelInstanceManager
import top.fifthlight.armorstand.util.RendererManager
import top.fifthlight.blazerod.api.render.ScheduledRenderer
import top.fifthlight.blazerod.api.resource.CameraTransform
import top.fifthlight.blazerod.api.resource.ModelInstance
import top.fifthlight.blazerod.model.Camera
import top.fifthlight.blazerod.model.HumanoidTag
import java.lang.ref.WeakReference
import java.util.*

object PlayerRenderer {
    private const val NANOSECONDS_PER_SECOND = 1_000_000_000L
    private var renderingWorld = false

    private var prevModelItem = WeakReference<ModelInstanceManager.ModelInstanceItem.Model?>(null)
    val selectedCameraIndex = MutableStateFlow<Int?>(null)
    private val _totalCameras = MutableStateFlow<List<Camera>?>(listOf())
    val totalCameras = _totalCameras.asStateFlow()
    private var cameraTransform: CameraTransform? = null

    @JvmStatic
    fun getCurrentCameraTransform(): CameraTransform? {
        cameraTransform?.let { return it }
        val entry = ModelInstanceManager.getSelfItem(load = false) ?: return null
        if (prevModelItem.get() != entry) {
            selectedCameraIndex.value = null
            if (entry is ModelInstanceManager.ModelInstanceItem.Model) {
                _totalCameras.value = entry.instance.scene.cameras
                prevModelItem = WeakReference(entry)
            } else {
                _totalCameras.value = listOf()
            }
            return null
        }

        val selectedIndex = selectedCameraIndex.value ?: return null
        val instance = entry.instance

        return instance.getCameraTransform(selectedIndex).also {
            cameraTransform = it
        } ?: run {
            selectedCameraIndex.value = null
            null
        }
    }

    fun startRenderWorld() {
        renderingWorld = true
    }

    private val matrix = Matrix4f()

    private val handWorldMatrix = Matrix4f()
    private val handWorldNoScaleMatrix = Matrix4f()
    private val handWorldPos = Vector3f()
    private val handWorldRot = Quaternionf()
    private val itemLocalMatrix = Matrix4f()
    private val itemNormalMatrix = Matrix3f()

    private fun renderHeldItem(
        instance: ModelInstance,
        player: AbstractClientPlayer?,
        itemStack: ItemStack?,
        displayContext: ItemDisplayContext,
        tag: HumanoidTag,
        matrixStack: PoseStack,
        consumers: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        if (itemStack == null || itemStack.isEmpty) {
            return
        }

        val config = ConfigHolder.config.value
        val node = instance.scene.humanoidTagMap[tag] ?: return
        instance.copyNodeWorldTransform(node.nodeIndex, handWorldMatrix)
        handWorldMatrix.getTranslation(handWorldPos)
        handWorldMatrix.getUnnormalizedRotation(handWorldRot)
        handWorldRot.normalize()
        handWorldNoScaleMatrix.translationRotate(
            handWorldPos.x,
            handWorldPos.y,
            handWorldPos.z,
            handWorldRot.x,
            handWorldRot.y,
            handWorldRot.z,
            handWorldRot.w,
        )

        itemLocalMatrix.identity()
        itemLocalMatrix.scale(config.modelScale)
        instance.scene.renderTransform?.applyOnMatrix(itemLocalMatrix)
        itemLocalMatrix.mul(handWorldNoScaleMatrix)

        itemLocalMatrix.rotateX(Math.toRadians(config.heldItemRotX.toDouble()).toFloat())
        itemLocalMatrix.rotateY(Math.toRadians(config.heldItemRotY.toDouble()).toFloat())
        itemLocalMatrix.rotateZ(Math.toRadians(config.heldItemRotZ.toDouble()).toFloat())
        val handSign = if (tag == HumanoidTag.LEFT_HAND) -1f else 1f
        itemLocalMatrix.translate(
            handSign * config.heldItemOffsetX,
            config.heldItemOffsetY,
            config.heldItemOffsetZ,
        )
        itemLocalMatrix.getTranslation(handWorldPos)
        itemLocalMatrix.getUnnormalizedRotation(handWorldRot)
        handWorldRot.normalize()
        itemLocalMatrix.translationRotate(
            handWorldPos.x,
            handWorldPos.y,
            handWorldPos.z,
            handWorldRot.x,
            handWorldRot.y,
            handWorldRot.z,
            handWorldRot.w,
        )

        matrixStack.pushPose()
        matrixStack.last().pose().mul(itemLocalMatrix)
        matrixStack.scale(config.heldItemScale, config.heldItemScale, config.heldItemScale)
        matrixStack.last().normal().set(
            itemNormalMatrix.set(matrixStack.last().pose()).invert().transpose()
        )
        if (player != null) {
            Minecraft.getInstance().itemRenderer.renderStatic(
                player,
                itemStack,
                displayContext,
                false,
                matrixStack,
                consumers,
                player.level(),
                light,
                overlay,
                0,
            )
        }
        matrixStack.popPose()
    }

    @JvmStatic
    fun updatePlayer(
        player: AbstractClientPlayer,
        state: PlayerRenderState,
    ) {
        val uuid = player.uuid
        val entry = ModelInstanceManager.get(uuid, System.nanoTime())
        if (entry !is ModelInstanceManager.ModelInstanceItem.Model) {
            return
        }

        val controller = entry.controller
        controller.update(uuid, player, state)
    }

    @JvmStatic
    fun appendPlayer(
        uuid: UUID,
        vanillaState: PlayerRenderState,
        matrixStack: PoseStack,
        consumers: MultiBufferSource,
        light: Int,
        overlay: Int,
    ): Boolean {
        val entry = ModelInstanceManager.get(uuid, System.nanoTime())
        if (entry !is ModelInstanceManager.ModelInstanceItem.Model) {
            return false
        }

        val controller = entry.controller
        val instance = entry.instance

        val time = System.nanoTime().toFloat() / NANOSECONDS_PER_SECOND.toFloat()
        controller.apply(uuid, instance, vanillaState)
        instance.updateRenderData(time)

        val playerEntity = Minecraft.getInstance().level?.getPlayerByUUID(uuid) as? AbstractClientPlayer
        val mainHandStack = playerEntity?.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
        val offHandStack = playerEntity?.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND)
        val rightHandStack: ItemStack? = if (vanillaState.mainArm == net.minecraft.world.entity.HumanoidArm.RIGHT) mainHandStack else offHandStack
        val leftHandStack: ItemStack? = if (vanillaState.mainArm == net.minecraft.world.entity.HumanoidArm.RIGHT) offHandStack else mainHandStack

        val backupItem = matrixStack.last().copy()
        matrixStack.popPose()
        matrixStack.pushPose()

        if (ArmorStandClient.instance.debugBone) {
            instance.debugRender(matrixStack.last().pose(), consumers, time)
        } else {
            matrix.set(matrixStack.last().pose())
            matrix.scale(ConfigHolder.config.value.modelScale)
            val currentRenderer = RendererManager.currentRenderer
            val task = instance.createRenderTask(matrix, light, overlay)
            if (currentRenderer is ScheduledRenderer<*, *> && renderingWorld) {
                currentRenderer.schedule(task)
            } else {
                val mainTarget = Minecraft.getInstance().mainRenderTarget
                val colorFrameBuffer = RenderSystem.outputColorTextureOverride ?: mainTarget.colorTextureView!!
                val depthFrameBuffer = RenderSystem.outputDepthTextureOverride ?: mainTarget.depthTextureView
                currentRenderer.render(
                    colorFrameBuffer = colorFrameBuffer,
                    depthFrameBuffer = depthFrameBuffer,
                    scene = instance.scene,
                    task = task,
                )
                task.release()
            }
            
            renderHeldItem(
                instance = instance,
                player = playerEntity,
                itemStack = rightHandStack,
                displayContext = ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                tag = HumanoidTag.RIGHT_HAND,
                matrixStack = matrixStack,
                consumers = consumers,
                light = light,
                overlay = overlay,
            )
            renderHeldItem(
                instance = instance,
                player = playerEntity,
                itemStack = leftHandStack,
                displayContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                tag = HumanoidTag.LEFT_HAND,
                matrixStack = matrixStack,
                consumers = consumers,
                light = light,
                overlay = overlay,
            )
        }

        matrixStack.popPose()
        matrixStack.pushPose()
        matrixStack.last().apply {
            pose().set(backupItem.pose())
            normal().set(backupItem.normal())
        }
        return true
    }

    fun executeDraw() {
        renderingWorld = false
        val mainTarget = Minecraft.getInstance().mainRenderTarget
        RendererManager.currentRendererScheduled?.let { renderer ->
            val colorFrameBuffer = RenderSystem.outputColorTextureOverride ?: mainTarget.colorTextureView!!
            val depthFrameBuffer = RenderSystem.outputDepthTextureOverride ?: mainTarget.depthTextureView
            renderer.executeTasks(colorFrameBuffer, depthFrameBuffer)
        }
    }

    fun endFrame() {
        cameraTransform = null
    }
}
