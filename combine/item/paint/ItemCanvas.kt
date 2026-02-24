package top.fifthlight.combine.item.paint

import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize

interface ItemCanvas : Canvas {
    fun drawItemStack(offset: IntOffset, size: IntSize = IntSize(16), stack: ItemStack)
}

inline fun Canvas.item(crossinline block: (canvas: ItemCanvas) -> Unit) = block(this as ItemCanvas)
