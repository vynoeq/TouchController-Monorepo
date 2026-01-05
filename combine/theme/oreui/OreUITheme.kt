package top.fifthlight.combine.theme.oreui

import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.Drawable
import top.fifthlight.combine.theme.Theme
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize

private data class OutlineDrawable(
    val inner: Drawable,
    val color: Color = Colors.WHITE,
): Drawable {
    override val size
        get() = inner.size
    override val padding: IntPadding
        get() = inner.padding

    override fun Canvas.draw(
        dstRect: IntRect,
        tint: Color,
    ) {
        with(inner) {
            draw(dstRect = dstRect, tint = tint)
        }
        drawRect(
            offset = dstRect.offset + IntOffset(-1),
            size = dstRect.size + 2,
            color = color,
        )
    }
}

val OreUITheme = run {
    val textures = OreUITexturesFactory.of()

    Theme(
        drawables = Theme.Drawables(
            button = DrawableSet(
                normal = textures.widget_button_button,
                hover = textures.widget_button_button_hover,
                focus = OutlineDrawable(textures.widget_button_button_focus),
                active = textures.widget_button_button_active,
                disabled = textures.widget_button_button_disabled,
            ),
            guideButton = DrawableSet(
                normal = textures.widget_button_button_guide,
                hover = textures.widget_button_button_guide_hover,
                focus = OutlineDrawable(textures.widget_button_button_guide), // TODO
                active = textures.widget_button_button_guide_active,
            ),
            warningButton = DrawableSet(
                normal = textures.widget_button_button_warning,
                hover = textures.widget_button_button_warning_hover,
                focus = OutlineDrawable(textures.widget_button_button_warning), // TODO
                active = textures.widget_button_button_warning_active,
            ),
            itemGridBackground = textures.background_backpack,
        ),
        colors = Theme.Colors(
            button = ColorTheme.light,
        ),
    )
}
