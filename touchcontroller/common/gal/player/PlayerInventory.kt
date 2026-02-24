package top.fifthlight.touchcontroller.common.gal.player

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.item.data.ItemStack

data class PlayerInventory(
    val main: PersistentList<ItemStack>,
    val armor: PersistentList<ItemStack>,
    val offHand: ItemStack?,
)
