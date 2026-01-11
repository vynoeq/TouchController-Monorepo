package top.fifthlight.armorstand.ui.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.CommonColors
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.manage.model.ModelItem
import top.fifthlight.armorstand.util.BlockableEventLoopDispatcher

class ModelButton(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    private val modelItem: ModelItem,
    private val font: Font,
    private val padding: Insets = Insets(),
    onPressAction: (ModelItem) -> Unit,
    private val onFavoriteAction: (ModelItem) -> Unit,
) : Button(
    x,
    y,
    width,
    height,
    Component.literal(modelItem.name),
    { onPressAction.invoke(modelItem) },
    DEFAULT_NARRATION,
), AutoCloseable {
    companion object {
        private val STAR_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("armorstand", "star")
        private val STAR_EMPTY_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("armorstand", "star_empty")
        private val STAR_HOVERED_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("armorstand", "star_hovered")
        private val STAR_EMPTY_HOVERED_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("armorstand", "star_empty_hovered")
        private const val STAR_ICON_SIZE = 9
        private const val STAR_ICON_PADDING = 4
    }

    private var closed = false
    private fun requireOpen() = require(!closed) { "Model button already closed" }

    private val scope = CoroutineScope(BlockableEventLoopDispatcher(Minecraft.getInstance()) + Job())
    private var checked = false
    private val modelIcon = ModelIcon(modelItem)

    init {
        scope.launch {
            ConfigHolder.config.map { it.modelPath }.distinctUntilChanged().collect {
                checked = it == modelItem.path
            }
        }
    }

    private val favoriteButtonXRange
        get() = x + width - STAR_ICON_SIZE - STAR_ICON_PADDING until x + width - STAR_ICON_PADDING
    private val favoriteButtonYRange
        get() = y + STAR_ICON_PADDING until y + STAR_ICON_SIZE + STAR_ICON_PADDING

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val clickedFavoriteButton = mouseX.toInt() in favoriteButtonXRange && mouseY.toInt() in favoriteButtonYRange
        if (active && visible && isValidClickButton(button) && clickedFavoriteButton) {
            playDownSound(Minecraft.getInstance().soundManager)
            onFavoriteAction.invoke(modelItem)
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        requireOpen()
        if (active && checked) {
            graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0x66000000u.toInt(),
            )
        }
        if (active && isHovered) {
            graphics.renderOutline(
                x,
                y,
                width,
                height,
                0x99000000u.toInt(),
            )
        } else if (isHovered) {
            graphics.renderOutline(
                x,
                y,
                width,
                height,
                0x44000000u.toInt(),
            )
        }
        val mouseInFavoriteIcon = mouseX in favoriteButtonXRange && mouseY in favoriteButtonYRange
        if (modelItem.favorite) {
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                if (mouseInFavoriteIcon) {
                    STAR_HOVERED_ICON
                } else {
                    STAR_ICON
                },
                favoriteButtonXRange.first,
                favoriteButtonYRange.first,
                STAR_ICON_SIZE,
                STAR_ICON_SIZE,
            )
        } else if (isHovered) {
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                if (mouseInFavoriteIcon) {
                    STAR_EMPTY_HOVERED_ICON
                } else {
                    STAR_EMPTY_ICON
                },
                favoriteButtonXRange.first,
                favoriteButtonYRange.first,
                STAR_ICON_SIZE,
                STAR_ICON_SIZE,
            )
        }

        val top = y + padding.top
        val bottom = y + height - padding.bottom
        val left = x + padding.left
        val right = x + width - padding.right

        val imageBottom = bottom - font.lineHeight - 8
        val imageWidth = right - left
        val imageHeight = imageBottom - top
        modelIcon.setPosition(left, top)
        modelIcon.setDimensions(imageWidth, imageHeight)
        modelIcon.render(graphics, mouseX, mouseY, deltaTicks)

        renderScrollingString(
            graphics,
            font,
            message,
            left,
            bottom - font.lineHeight,
            right,
            bottom,
            CommonColors.WHITE,
        )
    }

    override fun close() {
        scope.cancel()
        modelIcon.close()
        closed = true
    }
}
