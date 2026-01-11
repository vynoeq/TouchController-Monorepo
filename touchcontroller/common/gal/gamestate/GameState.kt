package top.fifthlight.touchcontroller.common.gal.gamestate

import top.fifthlight.mergetools.api.ExpectFactory

data class GameState(
    val inGame: Boolean,
    val inGui: Boolean,
    val perspective: CameraPerspective,
)

interface GameStateProvider {
    fun currentState(): GameState

    @ExpectFactory
    interface Factory {
        fun of(): GameStateProvider
    }
}
