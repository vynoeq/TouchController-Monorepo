package top.fifthlight.touchcontroller.common.ui.config.tab.status

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.config.tab.status.model.StatusTabModel
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

object StatusTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_STATUS_TITLE,
        group = null,
        index = 0,
    )

    @Composable
    override fun Content() {
        val tabModel = remember { StatusTabModel() }
        val uiState by tabModel.uiState.collectAsState()
        Column(
            modifier = Modifier
                .padding(8)
                .verticalScroll(background = LocalTouchControllerTheme.current.background)
                .fillMaxSize(),
        ) {
            uiState.currentPlatform?.let {
                Text(Text.format(Texts.SCREEN_CONFIG_STATUS_PLATFORM, it))
            } ?: run {
                Text(Text.format(Texts.SCREEN_CONFIG_STATUS_PLATFORM, Text.translatable(Texts.SCREEN_CONFIG_STATUS_PLATFORM_UNAVAILABLE)))
            }
        }
    }
}
