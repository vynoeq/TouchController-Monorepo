package top.fifthlight.touchcontroller.common.config.condition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget

@Serializable
@SerialName("select_entity")
data class SelectEntityLayerConditionKey(
    val entityType: EntityType,
) : LayerConditions.Key {
    override fun isFulfilled(input: LayerConditionInput): Boolean =
        (input.crosshairTarget as? CrosshairTarget.Entity)?.entityType == entityType
}
