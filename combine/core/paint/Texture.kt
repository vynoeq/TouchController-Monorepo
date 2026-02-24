package top.fifthlight.combine.paint

import androidx.compose.runtime.Immutable
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.Offset
import top.fifthlight.data.Rect
import top.fifthlight.mergetools.api.ExpectFactory

@Immutable
interface Texture : Drawable {
    fun draw(
        canvas: Canvas,
        dstRect: Rect,
        tint: Color = Colors.WHITE,
        srcRect: Rect = Rect(
            offset = Offset.ZERO,
            size = size.toSize(),
        ),
    )

    fun draw(
        canvas: Canvas,
        dstRect: IntRect,
        tint: Color = Colors.WHITE,
        srcRect: IntRect = IntRect(
            offset = IntOffset.ZERO,
            size = size,
        ),
    ) = draw(
        canvas = canvas,
        dstRect = dstRect.toRect(),
        tint = tint,
        srcRect = srcRect.toRect(),
    )

    @ExpectFactory
    interface Factory {
        fun create(
            namespace: String,
            id: String,
            width: Int,
            height: Int,
            padding: IntPadding,
        ): Texture
    }
}

@Immutable
interface BackgroundTexture : Drawable {
    override val padding: IntPadding
        get() = IntPadding.ZERO

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) = draw(canvas, dstRect, tint, 1f)
    fun draw(canvas: Canvas, dstRect: IntRect, tint: Color = Colors.WHITE, scale: Float) =
        draw(canvas, dstRect.toRect(), tint, scale)

    fun draw(canvas: Canvas, dstRect: Rect, tint: Color = Colors.WHITE, scale: Float)

    @ExpectFactory
    interface Factory {
        fun create(
            namespace: String,
            id: String,
            width: Int,
            height: Int,
        ): BackgroundTexture
    }
}
