package top.fifthlight.touchcontroller.common.event.tick

import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.model.ControllerHudModel

object TickEvents {
    private val keyBindingHandler: KeyBindingHandler = KeyBindingHandlerFactory.of()

    // Client side tick, neither server tick nor client render tick
    fun clientTick() {
        ControllerHudModel.timer.clientTick()
        keyBindingHandler.clientTick(ControllerHudModel.timer.clientTick)
    }
}
