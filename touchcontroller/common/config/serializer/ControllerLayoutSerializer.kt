package top.fifthlight.touchcontroller.config.serializer

class ControllerLayoutSerializer : KSerializer<ControllerLayout> {
    @OptIn(SealedSerializationApi::class)
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<ControllerWidget>>() {
        @OptIn(ExperimentalSerializationApi::class)
        override val serialName: String = "top.fifthlight.touchcontroller.config.ControllerLayout"
    }

    private val itemSerializer = serializer<LayoutLayer>()
    private val delegatedSerializer = ListSerializer(itemSerializer)

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: ControllerLayout) =
        delegatedSerializer.serialize(encoder, value)

    override fun deserialize(decoder: Decoder) =
        ControllerLayout(delegatedSerializer.deserialize(decoder).toPersistentList())
}