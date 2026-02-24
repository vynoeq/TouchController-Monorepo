package top.fifthlight.touchcontroller.common.ui.config.tab.layout.provider

import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.CustomControlLayoutTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.ManageControlPresetsTab

@ActualImpl(CustomTabProvider::class)
object CustomTabProviderImpl : CustomTabProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): CustomTabProvider = CustomTabProviderImpl

    override val customTab: Tab
        get() = CustomControlLayoutTab
    override val presetTab: Tab
        get() = ManageControlPresetsTab
}
