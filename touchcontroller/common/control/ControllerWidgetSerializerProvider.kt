package top.fifthlight.touchcontroller.common.control

import kotlinx.serialization.KSerializer
import top.fifthlight.mergetools.api.ExpectFactory

interface ControllerWidgetSerializerProvider : KSerializer<ControllerWidget> {
    @ExpectFactory
    interface Factory {
        fun of(): ControllerWidgetSerializerProvider
    }

    companion object : KSerializer<ControllerWidget> by ControllerWidgetSerializerProviderFactory.of()
}
