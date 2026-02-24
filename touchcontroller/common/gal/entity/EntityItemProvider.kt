package top.fifthlight.touchcontroller.common.gal.entity

import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ExpectFactory

interface EntityItemProvider {
    fun getEntityIconItem(entity: EntityType): Item?

    @ExpectFactory
    interface Factory {
        fun of(): EntityItemProvider
    }

    companion object : EntityItemProvider by EntityItemProviderFactory.of()
}
