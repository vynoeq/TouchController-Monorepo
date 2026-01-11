package top.fifthlight.touchcontroller.common.gal.item

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ExpectFactory

interface ItemSubclass {
    val id: String
    val configId: String
    val name: Text
    val items: PersistentList<Item>

    operator fun contains(item: Item): Boolean
}

interface ItemSubclassProvider {
    val itemSubclasses: PersistentList<ItemSubclass>

    @ExpectFactory
    interface Factory {
        fun of(): ItemSubclassProvider
    }
}
