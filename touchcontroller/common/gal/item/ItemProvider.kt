package top.fifthlight.touchcontroller.common.gal.item

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ExpectFactory

interface ItemProvider {
    val allItems: PersistentList<Item>

    @ExpectFactory
    interface Factory {
        fun of(): ItemProvider
    }
}
