package top.fifthlight.touchcontroller.common.control

import kotlinx.serialization.KSerializer

class ControllerWidgetSerializer : KSerializer<ControllerWidget> by ControllerWidgetSerializerProvider
