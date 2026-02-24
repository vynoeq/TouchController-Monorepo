package top.fifthlight.combine.theme.oreui

import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.Drawable
import top.fifthlight.combine.theme.Theme
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.ui.style.TextureSet
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize

private data class OutlineDrawable(
    val inner: Drawable,
    val color: Color = Colors.WHITE,
    val innerPadding: IntPadding = IntPadding.ZERO,
) : Drawable {
    override val size
        get() = inner.size
    override val padding: IntPadding
        get() = inner.padding

    override fun draw(
        canvas: Canvas,
        dstRect: IntRect,
        tint: Color,
    ) {
        inner.draw(
            canvas = canvas,
            dstRect = dstRect,
            tint = tint,
        )
        canvas.drawRect(
            offset = dstRect.offset + IntOffset(
                x = -1 + innerPadding.left,
                y = -1 + innerPadding.top,
            ),
            size = dstRect.size + IntSize(
                width = 2 - innerPadding.width,
                height = 2 - innerPadding.height,
            ),
            color = color,
        )
    }
}

val OreUITheme = run {
    Theme(
        drawables = Theme.Drawables(
            button = DrawableSet(
                normal = OreUITextures.widget_button_button,
                hover = OreUITextures.widget_button_button_hover,
                focus = OutlineDrawable(OreUITextures.widget_button_button_focus),
                active = OreUITextures.widget_button_button_active,
                disabled = OreUITextures.widget_button_button_disabled,
            ),
            guideButton = DrawableSet(
                normal = OreUITextures.widget_button_button_guide,
                hover = OreUITextures.widget_button_button_guide_hover,
                focus = OutlineDrawable(OreUITextures.widget_button_button_guide), // TODO
                active = OreUITextures.widget_button_button_guide_active,
            ),
            warningButton = DrawableSet(
                normal = OreUITextures.widget_button_button_warning,
                hover = OreUITextures.widget_button_button_warning_hover,
                focus = OutlineDrawable(OreUITextures.widget_button_button_warning), // TODO
                active = OreUITextures.widget_button_button_warning_active,
            ),

            switchFrame = DrawableSet(
                normal = OreUITextures.widget_switch_frame,
                disabled = OreUITextures.widget_switch_frame_disabled,
            ),
            switchHandle = DrawableSet(
                normal = OreUITextures.widget_handle_handle,
                hover = OreUITextures.widget_handle_handle_hover,
                focus = OutlineDrawable(OreUITextures.widget_handle_handle),
                disabled = OreUITextures.widget_handle_handle_disabled,
            ),
            switchBackground = TextureSet(
                normal = OreUITextures.widget_switch_switch,
                disabled = OreUITextures.widget_switch_switch_disabled,
            ),

            sliderHandle = DrawableSet(
                normal = OreUITextures.widget_handle_handle,
                hover = OreUITextures.widget_handle_handle_hover,
                focus = OutlineDrawable(OreUITextures.widget_handle_handle),
                disabled = OreUITextures.widget_handle_handle_disabled,
            ),
            sliderActiveTrack = DrawableSet(
                normal = OreUITextures.widget_slider_slider_active,
                disabled = OreUITextures.widget_slider_slider_active_disabled,
            ),
            sliderInactiveTrack = DrawableSet(
                normal = OreUITextures.widget_slider_slider_inactive,
                disabled = OreUITextures.widget_slider_slider_inactive_disabled,
            ),

            editText = DrawableSet(
                normal = OreUITextures.widget_textfield_textfield,
            ),

            itemGridBackground = OreUITextures.background_backpack,
        ),
        colors = Theme.Colors(
            button = ColorTheme.light,
        ),
    )
}
