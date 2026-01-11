package top.fifthlight.touchcontroller.common.event.connection

import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.gal.action.GameAction
import top.fifthlight.touchcontroller.common.gal.action.GameActionFactory
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.platform.proxy.ProxyPlatform

object ConnectionEvents {
    private val gameAction: GameAction = GameActionFactory.of()
    private val textFactory: TextFactory = TextFactoryFactory.of()

    fun onJoinedWorld() {
        val platform = PlatformProvider.platform
        if (platform == null) {
            gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED))
            val systemName = System.getProperty("os.name")
            val isLinux = systemName.startsWith("Linux", ignoreCase = true)
            val isWindows = systemName.startsWith("Windows", ignoreCase = true)
            if ((isLinux && PlatformProvider.isAndroid) || systemName.contains("Android", ignoreCase = true)) {
                gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED_ANDROID))
            } else if (isWindows) {
                gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED_WINDOWS))
            } else if (isLinux) {
                gameAction.sendMessage(textFactory.of(Texts.WARNING_PROXY_NOT_CONNECTED_LINUX))
            } else {
                gameAction.sendMessage(
                    textFactory.format(
                        Texts.WARNING_PROXY_NOT_CONNECTED_OS_NOT_SUPPORTED,
                        systemName
                    )
                )
            }
        } else if (platform is ProxyPlatform) {
            gameAction.sendMessage(textFactory.of(Texts.WARNING_LEGACY_UDP_PROXY_USED))
        }
    }
}
