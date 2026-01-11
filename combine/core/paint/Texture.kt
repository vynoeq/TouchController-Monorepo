package top.fifthlight.combine.paint

import androidx.compose.runtime.Immutable
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.Rect
import top.fifthlight.mergetools.api.ExpectFactory

@Immutable
interface Texture : Drawable {
    fun Canvas.draw(
        dstRect: Rect,
        tint: Color = Colors.WHITE,
        srcRect: Rect,
    )

    fun Canvas.draw(
        dstRect: IntRect,
        tint: Color = Colors.WHITE,
        srcRect: IntRect,
    ) {
        draw(
            dstRect = dstRect.toRect(),
            tint = tint,
            srcRect = srcRect.toRect(),
        )
    }

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

    fun Canvas.draw(dstRect: IntRect, tint: Color = Colors.WHITE, scale: Float) = draw(dstRect.toRect(), tint, scale)
    fun Canvas.draw(dstRect: Rect, tint: Color = Colors.WHITE, scale: Float)

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
