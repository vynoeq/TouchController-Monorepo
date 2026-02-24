package top.fifthlight.touchcontroller.common.layout

import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.withTranslate
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.layout.data.ContextInput
import top.fifthlight.touchcontroller.common.layout.data.ContextResult
import top.fifthlight.touchcontroller.common.layout.data.ContextStatus
import top.fifthlight.touchcontroller.common.layout.data.ContextTimer
import top.fifthlight.touchcontroller.common.layout.queue.DrawQueue
import top.fifthlight.touchcontroller.common.state.Pointer

data class Context(
    val windowSize: IntSize,
    val windowScaledSize: IntSize,
    val drawQueue: DrawQueue = DrawQueue(),
    val size: IntSize,
    val screenOffset: IntOffset,
    val opacity: Float = 1f,
    val pointers: MutableMap<Int, Pointer> = mutableMapOf(),
    val input: ContextInput = ContextInput(),
    val result: ContextResult = ContextResult(),
    val status: ContextStatus = ContextStatus(),
    val keyBindingHandler: KeyBindingHandler = KeyBindingHandler.Empty,
    val timer: ContextTimer = ContextTimer(),
    val config: ContextConfig = ContextConfig.Empty,
    val presetControlInfo: PresetControlInfo = PresetControlInfo(),
) {
    inline fun <reified T> transformDrawQueue(
        crossinline drawTransform: Canvas.(block: () -> Unit) -> Unit = { it() },
        crossinline contextTransform: Context.(DrawQueue) -> Context = { copy(drawQueue = it) },
        crossinline block: Context.() -> T,
    ): T {
        val newQueue = DrawQueue()
        val newContext = contextTransform(this, newQueue)
        val result = newContext.block()
        drawQueue.enqueue { canvas ->
            canvas.drawTransform {
                newQueue.execute(canvas)
            }
        }
        return result
    }

    inline fun <reified T> withOffset(offset: IntOffset, crossinline block: Context.() -> T): T =
        transformDrawQueue(
            drawTransform = { withTranslate(offset.x, offset.y) { it() } },
            contextTransform = { newQueue ->
                copy(
                    screenOffset = screenOffset + offset,
                    size = size - offset,
                    drawQueue = newQueue
                )
            },
            block
        )

    inline fun <reified T> withOffset(x: Int, y: Int, crossinline block: Context.() -> T): T =
        withOffset(IntOffset(x, y), block)

    inline fun <reified T> withSize(size: IntSize, crossinline block: Context.() -> T): T = copy(size = size).block()

    inline fun <reified T> withRect(x: Int, y: Int, width: Int, height: Int, crossinline block: Context.() -> T): T =
        transformDrawQueue(
            drawTransform = { withTranslate(x, y) { it() } },
            contextTransform = { newQueue ->
                copy(
                    screenOffset = screenOffset + IntOffset(x, y),
                    size = IntSize(width, height),
                    drawQueue = newQueue
                )
            },
            block
        )

    inline fun <reified T> withRect(offset: IntOffset, size: IntSize, crossinline block: Context.() -> T): T =
        transformDrawQueue(
            drawTransform = { withTranslate(offset.x, offset.y) { it() } },
            contextTransform = { newQueue ->
                copy(
                    screenOffset = screenOffset + offset,
                    size = size,
                    drawQueue = newQueue
                )
            },
            block
        )

    inline fun <reified T> withRect(rect: IntRect, crossinline block: Context.() -> T): T =
        withRect(rect.offset, rect.size, block)

    inline fun <reified T> withOpacity(opacity: Float, crossinline block: Context.() -> T): T =
        transformDrawQueue(
            contextTransform = { newQueue ->
                copy(
                    drawQueue = newQueue,
                    opacity = (this.opacity * opacity).coerceAtMost(1f)
                )
            },
            block = block
        )

    val Pointer.rawOffset: Offset
        get() = position * windowSize

    val Pointer.scaledOffset: Offset
        get() = position * windowScaledSize - screenOffset

    fun Pointer.inRect(size: IntSize): Boolean = scaledOffset in size

    fun getPointersInRect(size: IntSize): List<Pointer> = pointers.values.filter { it.inRect(size) }
}

inline fun <reified T> Context.withAlign(
    align: Align,
    size: IntSize,
    offset: IntOffset = IntOffset.ZERO,
    crossinline block: Context.() -> T,
): T = withRect(
    offset = align.alignOffset(windowSize = this.size, offset = offset, size = size),
    size = size,
    block = block
)
