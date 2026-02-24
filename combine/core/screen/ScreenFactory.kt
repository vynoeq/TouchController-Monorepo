package top.fifthlight.combine.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.data.Text
import top.fifthlight.mergetools.api.ExpectFactory

val LocalScreenFactory = staticCompositionLocalOf<ScreenFactory> { error("No ScreenFactory in context") }

interface ScreenFactory {
    fun openScreen(
        renderBackground: Boolean = false,
        title: Text,
        content: @Composable () -> Unit,
    )

    fun getScreen(
        parent: Any?,
        renderBackground: Boolean = false,
        title: Text,
        content: @Composable () -> Unit,
    ): Any

    @ExpectFactory
    interface Factory {
        fun of(): ScreenFactory
    }

    companion object : ScreenFactory by ScreenFactoryFactory.of()
}
