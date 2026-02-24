package top.fifthlight.touchcontroller.common.config.item

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.fifthlight.combine.data.*
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentType
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentTypeFactory
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclass
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclassProvider
import top.fifthlight.touchcontroller.common.serializer.ItemSerializer

@Immutable
@Serializable
@ConsistentCopyVisibility
data class ItemList private constructor(
    @SerialName("whitelist")
    private val _whitelist: ItemsList = ItemsList(),
    @SerialName("blacklist")
    private val _blacklist: ItemsList = ItemsList(),
    @SerialName("subclasses")
    private val _subclasses: ItemSubclassSet = ItemSubclassSet(),
    @SerialName("components")
    private val _components: ComponentTypesList = ComponentTypesList(),
) {
    constructor(
        whitelist: PersistentList<Item> = persistentListOf(),
        blacklist: PersistentList<Item> = persistentListOf(),
        components: PersistentList<ItemDataComponentType> = persistentListOf(),
        subclasses: PersistentSet<ItemSubclass> = persistentSetOf(),
    ) : this(
        _whitelist = ItemsList(whitelist),
        _blacklist = ItemsList(blacklist),
        _components = ComponentTypesList(components),
        _subclasses = ItemSubclassSet(subclasses),
    )

    val whitelist: PersistentList<Item>
        get() = _whitelist.items
    val blacklist: PersistentList<Item>
        get() = _blacklist.items
    val components: PersistentList<ItemDataComponentType>
        get() = _components.items
    val subclasses: PersistentSet<ItemSubclass>
        get() = _subclasses.items

    fun copy(
        whitelist: PersistentList<Item> = this.whitelist,
        blacklist: PersistentList<Item> = this.blacklist,
        components: PersistentList<ItemDataComponentType> = this.components,
        subclasses: PersistentSet<ItemSubclass> = this.subclasses,
    ) = ItemList(
        _whitelist = ItemsList(whitelist),
        _blacklist = ItemsList(blacklist),
        _components = ComponentTypesList(components),
        _subclasses = ItemSubclassSet(subclasses),
    )

    operator fun contains(item: Item) = when {
        blacklist.any { it.matches(item) } -> false
        whitelist.any { it.matches(item) } -> true
        components.any { item in it } -> true
        subclasses.any { item in it } -> true
        else -> false
    }
}

// Workaround of Kotlin serialization
@JvmInline
@Serializable(with = ItemsListSerializer::class)
value class ItemsList(val items: PersistentList<Item> = persistentListOf())

class ItemsListSerializer : KSerializer<ItemsList> {
    companion object {
        private val itemSerializer = ItemSerializer()
    }

    @OptIn(SealedSerializationApi::class)
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<PersistentList<Item>>()

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: ItemsList) {
        ListSerializer(itemSerializer).serialize(encoder, value.items)
    }

    override fun deserialize(decoder: Decoder): ItemsList {
        return ItemsList(ListSerializer(itemSerializer).deserialize(decoder).toPersistentList())
    }
}

@JvmInline
@Serializable(with = ItemDataComponentTypeSerializer::class)
value class ComponentTypesList(val items: PersistentList<ItemDataComponentType> = persistentListOf())

class ItemDataComponentTypeSerializer : KSerializer<ComponentTypesList> {
    @OptIn(SealedSerializationApi::class)
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<PersistentList<Item>>()

    private val itemSerializer = serializer<String>()

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: ComponentTypesList) {
        val ids = value.items.mapNotNull { it.id?.toString() }
        ListSerializer(itemSerializer).serialize(encoder, ids)
    }

    override fun deserialize(decoder: Decoder): ComponentTypesList {
        return ComponentTypesList(ListSerializer(itemSerializer).deserialize(decoder).mapNotNull {
            ItemDataComponentTypeFactory.of(Identifier(it))
        }.toPersistentList())
    }
}

@JvmInline
@Serializable(with = ItemSubclassSetSerializer::class)
value class ItemSubclassSet(val items: PersistentSet<ItemSubclass> = persistentSetOf())

class ItemSubclassSetSerializer : KSerializer<ItemSubclassSet> {
    companion object {
        private val allSubclasses = ItemSubclassProvider.itemSubclasses
    }

    @OptIn(SealedSerializationApi::class)
    private class PersistentSetDescriptor : SerialDescriptor by serialDescriptor<PersistentSet<Item>>()

    private val itemSerializer = serializer<String>()

    override val descriptor: SerialDescriptor = PersistentSetDescriptor()

    override fun serialize(encoder: Encoder, value: ItemSubclassSet) {
        val ids = value.items.map { it.configId }.toSet()
        SetSerializer(itemSerializer).serialize(encoder, ids)
    }

    override fun deserialize(decoder: Decoder): ItemSubclassSet {
        return ItemSubclassSet(SetSerializer(itemSerializer).deserialize(decoder).mapNotNull { id ->
            allSubclasses.firstOrNull { it.configId == id }
        }.toPersistentSet())
    }
}
