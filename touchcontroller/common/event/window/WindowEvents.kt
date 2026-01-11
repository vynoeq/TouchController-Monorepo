package top.fifthlight.touchcontroller.common.event.window

import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindowProvider
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.proxy.message.InitializeMessage

object WindowEvents {
    private val logger = LoggerFactory.getLogger(WindowEvents::class.java)
    private lateinit var windowProvider: PlatformWindowProvider
    val windowWidth: Int
        get() = windowProvider.windowWidth
    val windowHeight: Int
        get() = windowProvider.windowHeight

    private val mainThreadDispatcher by lazy {
        try {
            IxerisDispatcher(Class.forName("me.decce.ixeris.api.IxerisApi"))
        } catch (_: ClassNotFoundException) {
            EmptyDispatcher()
        }
    }

    fun onWindowCreated(windowProvider: PlatformWindowProvider) {
        this.windowProvider = windowProvider
        mainThreadDispatcher.execute {
            PlatformProvider.load(windowProvider)
            PlatformProvider.platform?.sendEvent(InitializeMessage)
            logger.info("Loaded platform on thread ${Thread.currentThread().name}")
        }
    }
}
