package top.fifthlight.touchcontroller.common.control.serializer

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedClassSerializer
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.ControllerWidgetSerializerProvider
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButton
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPad
import top.fifthlight.touchcontroller.common.control.widget.custom.CustomWidget
import top.fifthlight.touchcontroller.common.control.widget.joystick.Joystick

@ActualImpl(ControllerWidgetSerializerProvider::class)
@OptIn(InternalSerializationApi::class)
object ControllerWidgetSerializerProviderImpl : ControllerWidgetSerializerProvider,
    KSerializer<ControllerWidget> by SealedClassSerializer(
        serialName = "top.fifthlight.touchcontroller.common.control.ControllerWidget",
        baseClass = ControllerWidget::class,
        subclasses = arrayOf(BoatButton::class, CustomWidget::class, DPad::class, Joystick::class),
        subclassSerializers = arrayOf(
            BoatButton.serializer(),
            CustomWidget.serializer(),
            DPad.serializer(),
            Joystick.serializer()
        ),
    ) {
    @JvmStatic
    @ActualConstructor
    fun of(): ControllerWidgetSerializerProvider = ControllerWidgetSerializerProviderImpl
}
