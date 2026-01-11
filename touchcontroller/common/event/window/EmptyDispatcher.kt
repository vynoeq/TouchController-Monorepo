package top.fifthlight.touchcontroller.common.event.window

internal class EmptyDispatcher : MainThreadDispatcher {
    override fun execute(command: Runnable) = command.run()
}
