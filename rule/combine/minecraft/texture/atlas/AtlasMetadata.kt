package top.fifthlight.combine.resources.altas

import kotlinx.serialization.Serializable

@Serializable
data class AtlasMetadata(
    val width: Int,
    val height: Int,
    val textures: Map<String, PlacedTexture>,
)
