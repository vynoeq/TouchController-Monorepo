package top.fifthlight.touchcontroller.common.config.condition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput

@Serializable
@SerialName("builtin")
data class BuiltinLayerConditionKey(
    val condition: BuiltinLayerCondition,
) : LayerConditions.Key {
    override fun isFulfilled(input: LayerConditionInput): Boolean = condition in input.builtinCondition
}
