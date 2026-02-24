package top.fifthlight.touchcontroller.common.config.condition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions.Key
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import top.fifthlight.touchcontroller.common.serializer.ItemSerializer

@Serializable
@SerialName("holding_item")
data class HoldingItemLayerConditionKey(
    @Serializable(with = ItemSerializer::class)
    val item: Item,
) : Key {
    override fun isFulfilled(input: LayerConditionInput) = input.holdingItem(item)
}
