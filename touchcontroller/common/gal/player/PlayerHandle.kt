package top.fifthlight.touchcontroller.common.gal.player

import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.mergetools.api.ExpectFactory
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.entity.EntityType

interface PlayerHandle {
    fun matchesItemOnHand(item: Item): Boolean
    fun hasItemsOnHand(list: ItemList): Boolean
    fun changeLookDirection(deltaYaw: Double, deltaPitch: Double)
    var currentSelectedSlot: Int
    fun dropSlot(index: Int)
    fun getInventorySlot(index: Int): ItemStack
    fun getInventory(): PlayerInventory
    val isUsingItem: Boolean
    val onGround: Boolean
    var isFlying: Boolean
    val isSubmergedInWater: Boolean
    val isTouchingWater: Boolean
    var isSprinting: Boolean
    val isSneaking: Boolean
    val ridingEntityType: EntityType?
    val canFly: Boolean

    @ExpectFactory
    interface Factory {
        fun current(): PlayerHandle?
    }
}
