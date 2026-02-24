package top.fifthlight.touchcontroller.common.ui.config.tab.all

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.touchcontroller.common.ui.config.model.ConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.about.AboutTab
import top.fifthlight.touchcontroller.common.ui.config.tab.ItemTabs
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.general.ControlTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.DebugTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.RegularTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.TouchRingTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.CustomControlLayoutTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.ManageControlPresetsTab

fun getAllTabs(configScreenModel: ConfigScreenModel): PersistentList<Tab> {
    val itemTabs = ItemTabs(configScreenModel)
    return persistentListOf(
        AboutTab,
        ManageControlPresetsTab,
        CustomControlLayoutTab,
        RegularTab,
        ControlTab,
        TouchRingTab,
        DebugTab,
        itemTabs.usableItemsTab,
        itemTabs.showCrosshairItemsTab,
        itemTabs.crosshairAimingItemsTab,
    )
}

val allTabGroups = persistentListOf(
    TabGroup.LayoutGroup,
    TabGroup.GeneralGroup,
    TabGroup.ItemGroup,
)
