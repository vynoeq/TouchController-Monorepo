package top.fifthlight.touchcontroller.common.control.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BoatButtonSide {
    @SerialName("left")
    LEFT,

    @SerialName("right")
    RIGHT
}
