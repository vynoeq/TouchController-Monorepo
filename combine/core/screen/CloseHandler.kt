package top.fifthlight.combine.screen

import androidx.compose.runtime.staticCompositionLocalOf

val LocalCloseHandler = staticCompositionLocalOf<CloseHandler> { CloseHandler.Empty }

interface CloseHandler {
    fun close()

    object Empty : CloseHandler {
        override fun close() {}
    }
}
