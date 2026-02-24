package top.fifthlight.touchcontroller.version_26_1.gal

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.core.registries.BuiltInRegistries
import top.fifthlight.combine.backend.minecraft_26_1.toCombine
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.item.ItemProvider

@ActualImpl(ItemProvider::class)
object ItemProviderImpl: ItemProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ItemProvider = this

    override val allItems: PersistentList<Item> by lazy {
        BuiltInRegistries.ITEM.map { it.toCombine() }.toPersistentList()
    }
}
