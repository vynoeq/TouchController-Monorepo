package top.fifthlight.touchcontroller.common.config.condition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import kotlin.uuid.Uuid

@Serializable
@SerialName("custom")
data class CustomLayerConditionKey(
    val key: Uuid,
) : LayerConditions.Key {
    override fun isFulfilled(input: LayerConditionInput) = key in input.customCondition
}
