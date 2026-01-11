package top.fifthlight.touchcontroller.common.gal.feature

import top.fifthlight.mergetools.api.ExpectFactory

data class GameFeatures(
    val dualWield: Boolean,
    val entity: EntityFeatures
)

interface GameFeaturesProvider {
    val gameFeatures: GameFeatures

    @ExpectFactory
    interface Factory {
        fun of(): GameFeaturesProvider
    }
}
