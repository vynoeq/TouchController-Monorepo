package top.fifthlight.touchcontroller.common.ui.layer.state

import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer

data class LayerEditorScreenState(
    val name: String,
    val conditions: LayerConditions,
) {
    constructor(layer: LayoutLayer) : this(
        name = layer.name,
        conditions = layer.conditions,
    )

    fun edit(layer: LayoutLayer) = layer.copy(
        name = name,
        conditions = conditions,
    )
}
