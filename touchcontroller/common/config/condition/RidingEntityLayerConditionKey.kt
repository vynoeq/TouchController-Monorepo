package top.fifthlight.touchcontroller.common.config.condition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import top.fifthlight.touchcontroller.common.gal.entity.EntityType

@Serializable
@SerialName("riding_entity")
data class RidingEntityLayerConditionKey(
    val entityType: EntityType,
) : LayerConditions.Key {
    override fun isFulfilled(input: LayerConditionInput): Boolean = input.ridingEntity == entityType
}
