package top.fifthlight.tools.texteditor

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import top.fifthlight.tools.texteditor.ui.App
import java.nio.file.Path
import kotlin.io.path.Path

fun main(args: Array<String>) = application {
    val initialPath = args.firstOrNull()?.let { Path(it) }
    Window(
        title = "Text editor",
        onCloseRequest = ::exitApplication,
    ) {
        SideEffect {
            window.setSize(1000, 700)
        }
        MaterialTheme(
            colorScheme = darkColorScheme(),
        ) {
            App(window = window, initialPath = initialPath)
        }
    }
}
