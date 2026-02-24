package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.state

import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer

sealed class LayersTabState {
    data object Empty : LayersTabState()

    data class Delete(
        val index: Int,
        val layer: LayoutLayer,
    ) : LayersTabState()
}
