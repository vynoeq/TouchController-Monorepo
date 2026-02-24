package top.fifthlight.touchcontroller.common.layout

import top.fifthlight.touchcontroller.common.config.data.ControlConfig
import top.fifthlight.touchcontroller.common.config.data.TouchRingConfig
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle

interface ContextConfig {
    val touchRingConfig: TouchRingConfig
    val controlConfig: ControlConfig
    val showPointers: Boolean
    val quickHandSwap: Boolean

    fun isHandItemUsable(player: PlayerHandle): Boolean

    object Empty : ContextConfig {
        override val touchRingConfig = TouchRingConfig()
        override val controlConfig = ControlConfig()
        override val showPointers = false
        override val quickHandSwap = false

        override fun isHandItemUsable(player: PlayerHandle) = false
    }
}
