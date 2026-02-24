package top.fifthlight.touchcontroller.common.config

import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.data.ChatConfig
import top.fifthlight.touchcontroller.common.config.data.ControlConfig
import top.fifthlight.touchcontroller.common.config.data.DebugConfig
import top.fifthlight.touchcontroller.common.config.data.RegularConfig
import top.fifthlight.touchcontroller.common.config.data.TouchRingConfig
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider

@Serializable
data class GlobalConfig(
    val regular: RegularConfig = RegularConfig(),
    val control: ControlConfig = ControlConfig(),
    val touchRing: TouchRingConfig = TouchRingConfig(),
    val debug: DebugConfig = DebugConfig(),
    val item: ItemConfig = ItemConfig.default(DefaultItemListProvider),
    val preset: PresetConfig = PresetConfig.BuiltIn(),
    val chat: ChatConfig = ChatConfig(),
) {
    companion object {
        val default by lazy {
            GlobalConfig()
        }
    }
}
