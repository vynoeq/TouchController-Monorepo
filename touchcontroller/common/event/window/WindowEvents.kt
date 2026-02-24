package top.fifthlight.touchcontroller.common.event.window

import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindowProvider
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.proxy.message.InitializeMessage

object WindowEvents {
    private val logger = LoggerFactory.getLogger(WindowEvents::class.java)
    val windowWidth: Int
        get() = PlatformWindowProvider.windowWidth
    val windowHeight: Int
        get() = PlatformWindowProvider.windowHeight

    private val mainThreadDispatcher by lazy {
        try {
            IxerisDispatcher(Class.forName("me.decce.ixeris.api.IxerisApi"))
        } catch (_: ClassNotFoundException) {
            EmptyDispatcher()
        }
    }

    fun onWindowCreated() {
        mainThreadDispatcher.execute {
            PlatformProvider.load()
            PlatformProvider.platform?.sendEvent(InitializeMessage)
            logger.info("Loaded platform on thread ${Thread.currentThread().name}")
        }
    }
}
