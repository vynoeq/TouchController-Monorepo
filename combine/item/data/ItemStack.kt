package top.fifthlight.combine.item.data

import androidx.compose.runtime.Immutable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text
import top.fifthlight.mergetools.api.ExpectFactory

@Immutable
interface ItemStack {
    val amount: Int
    val id: Identifier
    val item: Item
    val isEmpty: Boolean
    val name: Text

    fun withAmount(amount: Int): ItemStack

    @ExpectFactory
    interface Factory {
        fun create(item: Item, amount: Int): ItemStack
        fun create(id: Identifier, amount: Int): ItemStack?
    }
}
