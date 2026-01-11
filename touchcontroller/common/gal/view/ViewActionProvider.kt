package top.fifthlight.touchcontroller.common.gal.view

import top.fifthlight.mergetools.api.ExpectFactory

interface ViewActionProvider {
    fun getCrosshairTarget(): CrosshairTarget?
    fun getCurrentBreakingProgress(): Float

    @ExpectFactory
    interface Factory {
        fun of(): ViewActionProvider
    }
}
