package top.fifthlight.touchcontroller.common.gal.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import top.fifthlight.mergetools.api.ExpectFactory

interface GameDispatcherProvider {
    val gameDispatcher: CoroutineDispatcher

    @ExpectFactory
    interface Factory {
        fun of(): GameDispatcherProvider
    }
}

val gameDispatcher by lazy {
    GameDispatcherProviderFactory.of().gameDispatcher
}
