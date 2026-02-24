package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.all

import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.LayersTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.PresetsTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.properties.PropertiesTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.WidgetsTab

val allCustomTabs = persistentListOf(
    PropertiesTab,
    WidgetsTab,
    LayersTab,
    PresetsTab,
)
