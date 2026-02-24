package top.fifthlight.touchcontroller.version_26_1.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory
import top.fifthlight.combine.data.Identifier
import net.minecraft.client.KeyMapping
import top.fifthlight.combine.backend.minecraft_26_1.CanvasImpl
import top.fifthlight.combine.backend.minecraft_26_1.toMinecraft
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.event.block.BlockBreakEvents
import top.fifthlight.touchcontroller.common.event.connection.ConnectionEvents
import top.fifthlight.touchcontroller.common.event.key.KeyEvents
import top.fifthlight.touchcontroller.common.event.render.RenderEvents
import top.fifthlight.touchcontroller.common.event.tick.TickEvents
import top.fifthlight.touchcontroller.common.event.window.WindowEvents
import top.fifthlight.touchcontroller.common.model.ControllerHudModel
import top.fifthlight.touchcontroller.common.model.TouchControllerLoadStatus
import top.fifthlight.touchcontroller.version_26_1.gal.GameConfigEditorImpl
import top.fifthlight.touchcontroller.version_26_1.gal.KeyBindingStateImpl
import top.fifthlight.touchcontroller.version_26_1.gal.PlatformWindowProviderImpl

class TouchController : ClientModInitializer {
    private val logger = LoggerFactory.getLogger(TouchController::class.java)

    companion object {
        @JvmStatic
        var isInEmulatedSetDown = false
    }

    override fun onInitializeClient() {
        logger.info("Loading TouchController…")

        initialize()

        TouchControllerLoadStatus.isLoaded = true
    }

    private fun initialize() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.MISC_OVERLAYS,
            Identifier.of(BuildInfo.MOD_ID, "hud").toMinecraft()
        ) { drawContext, partialTicks ->
            val client = Minecraft.getInstance()
            if (!client.options.hideGui) {
                val canvas = CanvasImpl(drawContext)
                RenderEvents.onHudRender(canvas)
            }
        }

        KeyEvents.addHandler { state ->
            val vanillaState = state as KeyBindingStateImpl
            val vanillaKeyBinding = vanillaState.keyBinding
            if (vanillaKeyBinding.javaClass != KeyMapping::class.java) {
                isInEmulatedSetDown = true
                vanillaState.keyBinding.isDown = true
                isInEmulatedSetDown = false
            }
        }

        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register { _, _ ->
            ControllerHudModel.result.showBlockOutline
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            TickEvents.clientTick()
        }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            ConnectionEvents.onJoinedWorld()
        }
        ClientLifecycleEvents.CLIENT_STARTED.register {
            GlobalConfigHolder.load()
            WindowEvents.onWindowCreated()
            GameConfigEditorImpl.executePendingCallback()
        }
        ClientPlayerBlockBreakEvents.AFTER.register { _, _, _, _ ->
            BlockBreakEvents.afterBlockBreak()
        }
    }
}
