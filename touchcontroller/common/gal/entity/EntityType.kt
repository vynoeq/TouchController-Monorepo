package top.fifthlight.touchcontroller.common.gal.entity

import kotlinx.collections.immutable.PersistentList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text
import top.fifthlight.mergetools.api.ExpectFactory

@Serializable(EntityTypeSerializer::class)
abstract class EntityType {
    abstract val identifier: Identifier
    abstract val name: Text
}

class EntityTypeSerializer : KSerializer<EntityType> {
    private val delegate = Identifier.serializer()
    private val types = EntityTypeProvider.allTypes.associateBy { it.identifier }

    override val descriptor: SerialDescriptor = SerialDescriptor(
        serialName = "top.fifthlight.touchcontroller.common.gal.entity.EntityType",
        original = serialDescriptor<Identifier>(),
    )

    override fun serialize(
        encoder: Encoder,
        value: EntityType,
    ) = encoder.encodeSerializableValue(delegate, value.identifier)

    override fun deserialize(decoder: Decoder) = decoder.decodeSerializableValue(delegate)
        .let { identifier -> types[identifier] ?: error("Bad entity type: $identifier") }
}

interface EntityTypeProvider {
    val allTypes: PersistentList<EntityType>

    val player: EntityType
    val minecart: EntityType?
    val pig: EntityType?
    val llama: EntityType?
    val strider: EntityType?

    val boats: PersistentList<EntityType>
    val horses: PersistentList<EntityType>
    val camel: PersistentList<EntityType>

    @ExpectFactory
    interface Factory {
        fun of(): EntityTypeProvider
    }

    companion object : EntityTypeProvider by EntityTypeProviderFactory.of()
}
