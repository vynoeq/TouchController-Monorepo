package top.fifthlight.touchcontroller.common.gal.creativetab

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.mergetools.api.ExpectFactory
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle

interface CreativeTabsProvider {
    interface CreativeTab {
        enum class Type {
            CATEGORY,
            SEARCH,
            SURVIVAL_INVENTORY,
        }

        val type: Type
        val name: Text
        val icon: ItemStack
        val items: PersistentList<ItemStack>
    }

    fun getCreativeTabs(player: PlayerHandle): PersistentList<CreativeTab>

    @ExpectFactory
    interface Factory {
        fun of(): CreativeTabsProvider
    }
}
