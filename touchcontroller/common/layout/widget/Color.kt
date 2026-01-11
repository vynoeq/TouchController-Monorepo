package top.fifthlight.touchcontroller.common.layout.widget

import top.fifthlight.combine.paint.Color
import top.fifthlight.data.IntOffset
import top.fifthlight.touchcontroller.common.layout.Context

fun Context.Color(color: Color) {
    drawQueue.enqueue { canvas ->
        canvas.fillRect(IntOffset.ZERO, size, color)
    }
}
