package top.fifthlight.touchcontroller.common.gal.item

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ExpectFactory

interface ItemDataComponentType {
    val id: Identifier?
    val allItems: PersistentList<Item>

    operator fun contains(item: Item): Boolean

    @ExpectFactory
    interface Factory {
        fun of(id: Identifier): ItemDataComponentType?
    }
}

interface ItemDataComponentTypeProvider {
    val supportDataComponents: Boolean

    val allComponents: PersistentList<ItemDataComponentType>

    @ExpectFactory
    interface Factory {
        fun of(): ItemDataComponentTypeProvider
    }
}
