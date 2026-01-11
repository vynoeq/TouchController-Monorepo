package top.fifthlight.touchcontroller.common.layout.widget

import top.fifthlight.combine.paint.Color
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.data.BoatButtonSide
import top.fifthlight.touchcontroller.common.layout.Context
import kotlin.uuid.Uuid

fun Context.BoatButton(
    id: Uuid,
    classic: Boolean,
    textureSet: TextureSet,
    side: BoatButtonSide,
) {
    val (_, clicked) = Button(id) { clicked ->
        if (classic) {
            if (clicked) {
                Texture(textureSet.up, tint = Color(0xFFAAAAAAu))
            } else {
                Texture(textureSet.up)
            }
        } else {
            if (clicked) {
                Texture(textureSet.upActive)
            } else {
                Texture(textureSet.up)
            }
        }
    }
    if (clicked) {
        when (side) {
            BoatButtonSide.LEFT -> result.boatLeft = true
            BoatButtonSide.RIGHT -> result.boatRight = true
        }
    }
}
