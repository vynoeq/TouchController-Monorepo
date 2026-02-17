package top.fifthlight.armorstand

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.PlayerRenderState
import org.joml.Matrix4f
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.state.ModelInstanceManager
import top.fifthlight.armorstand.util.RendererManager
import top.fifthlight.blazerod.model.Camera
import top.fifthlight.blazerod.render.api.resource.CameraTransform
import top.fifthlight.blazerod.render.version_1_21_8.api.render.ScheduledRenderer
import top.fifthlight.blazerod.render.version_1_21_8.api.resource.debugRender
import java.lang.ref.WeakReference
import java.util.*

object PlayerRenderer {
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

        controller.apply(uuid, instance, vanillaState)
        instance.updateRenderData()

        val backupItem = matrixStack.last().copy()
        matrixStack.popPose()
        matrixStack.pushPose()

        if (ArmorStandClient.instance.debugBone) {
            instance.debugRender(matrixStack.last().pose(), consumers)
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
