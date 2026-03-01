package top.fifthlight.touchcontroller.common.platform

import top.fifthlight.combine.data.Text
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage

interface Platform {
    val name: Text

    fun resize(width: Int, height: Int) {}
    fun pollEvent(): ProxyMessage?
    fun sendEvent(message: ProxyMessage)
}
