package top.fifthlight.touchcontroller.common.ui.config.tab.status.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.ui.config.tab.status.state.StateTabState
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class StatusTabModel: TouchControllerScreenModel() {
    private val _uiState = MutableStateFlow(StateTabState())
    val uiState = _uiState.asStateFlow()

    init {
        coroutineScope.launch {
            _uiState.update {
                it.copy(
                    currentPlatform = PlatformProvider.platform?.name,
                )
            }
        }
    }
}
