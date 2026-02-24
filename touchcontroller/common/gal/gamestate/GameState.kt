package top.fifthlight.touchcontroller.common.gal.gamestate

import top.fifthlight.mergetools.api.ExpectFactory

interface GameState {
    val inGame: Boolean
    val inGui: Boolean
    val perspective: CameraPerspective

    companion object : GameState by GameStateProviderFactory.of().currentState
}

interface GameStateProvider {
    val currentState: GameState

    @ExpectFactory
    interface Factory {
        fun of(): GameStateProvider
    }
}
