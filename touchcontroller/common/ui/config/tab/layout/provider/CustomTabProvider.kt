package top.fifthlight.touchcontroller.common.ui.config.tab.layout.provider

import top.fifthlight.mergetools.api.ExpectFactory
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab

interface CustomTabProvider {
    val customTab: Tab
    val presetTab: Tab

    @ExpectFactory
    interface Factory {
        fun of(): CustomTabProvider
    }

    companion object : CustomTabProvider by CustomTabProviderFactory.of()
}
