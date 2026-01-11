package top.fifthlight.combine.item.data

import androidx.compose.runtime.Immutable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.mergetools.api.ExpectFactory

@Immutable
interface Item {
    val id: Identifier
    fun matches(other: Item): Boolean = equals(other)

    fun toStack() = toStack(1)
    fun toStack(amount: Int): ItemStack = ItemStackFactory.create(this, amount)

    @ExpectFactory
    interface Factory {
        fun create(id: Identifier): Item?
    }
}
