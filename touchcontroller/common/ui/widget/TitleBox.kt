package top.fifthlight.touchcontroller.common.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.LocalColorTheme
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.BoxScope
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
fun TitleBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(top = 2, bottom = 3)
            .border(LocalTouchControllerTheme.current.titleBoxBackground)
            .then(modifier),
        alignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalColorTheme provides ColorTheme.light
        ) {
            content()
        }
    }
}
