package top.fifthlight.touchcontroller.common.event.block

import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.model.ControllerHudModel

object BlockBreakEvents {
    fun afterBlockBreak() {
        if (GlobalConfigHolder.config.value.regular.vibration) {
            ControllerHudModel.status.vibrate = true
        }
    }
}
