package top.fifthlight.touchcontroller.common.ui.model

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import top.fifthlight.combine.util.dispatcher.GameDispatcherProviderFactory

abstract class TouchControllerScreenModel : ScreenModel {
    private val gameDispatcher: CoroutineDispatcher = GameDispatcherProviderFactory.of().gameDispatcher

    val coroutineScope = CoroutineScope(SupervisorJob() + gameDispatcher)

    override fun onDispose() {
        super.onDispose()
        coroutineScope.cancel()
    }
}
