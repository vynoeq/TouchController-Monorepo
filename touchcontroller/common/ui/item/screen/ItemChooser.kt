package top.fifthlight.touchcontroller.common.ui.item.screen

import androidx.compose.runtime.Composable
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.navigation.TouchControllerNavigator

@Composable
fun ItemChooser(
    onItemChosen: (Item) -> Unit,
) {
    if (!GameState.inGame) {
        Box(
            modifier = Modifier
                .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                .fillMaxSize(),
            alignment = Alignment.Center,
        ) {
            Text(Text.translatable(Texts.SCREEN_ITEM_LIST_WARNING_NOT_IN_GAME))
        }
        return
    }

    TouchControllerNavigator(ItemListChooseScreen(onItemChosen))
}
