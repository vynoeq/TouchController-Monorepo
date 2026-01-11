package top.fifthlight.touchcontroller.common.event.window

internal class IxerisDispatcher(clazz: Class<*>) : MainThreadDispatcher {
    init {
        require(clazz.canonicalName == "me.decce.ixeris.api.IxerisApi") { "Bad ixeris API class" }
    }

    private val instance = clazz.getMethod("getInstance").invoke(null)
    private val runLaterOnMainThreadMethod = clazz.getMethod("runLaterOnMainThread", Runnable::class.java)

    override fun execute(command: Runnable) {
        runLaterOnMainThreadMethod.invoke(instance, command)
    }
}
