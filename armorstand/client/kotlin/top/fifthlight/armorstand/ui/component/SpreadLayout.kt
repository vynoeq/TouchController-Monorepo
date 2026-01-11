package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.AbstractLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import java.util.function.Consumer

class SpreadLayout(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    var direction: Direction = Direction.HORIZONTAL,
    var gap: Int = 0,
    var padding: Insets = Insets.ZERO,
) : AbstractLayout(x, y, width, height), ResizableLayout {

    enum class Direction {
        HORIZONTAL,
        VERTICAL,
    }

    private class Element<T : LayoutElement>(
        private val inner: T,
        positioner: LayoutSettings,
        var weight: Int = 1,
        private val onSizeChanged: (widget: T, width: Int, height: Int) -> Unit = { _, _, _ -> },
    ) : AbstractChildWrapper(inner, positioner) {
        fun setSize(width: Int, height: Int) = onSizeChanged(
            inner,
            width - layoutSettings.paddingLeft - layoutSettings.paddingRight,
            height - layoutSettings.paddingTop - layoutSettings.paddingBottom
        )
    }

    private val elements = mutableListOf<Element<*>>()

    override fun visitChildren(consumer: Consumer<LayoutElement>) {
        elements.forEach { consumer.accept(it.child) }
    }

    override fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun <T : LayoutElement> add(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        weight: Int = 1,
        onSizeChanged: (widget: T, width: Int, height: Int) -> Unit
    ) {
        elements.add(Element(widget, layoutSettings, weight.coerceAtLeast(1), onSizeChanged))
    }

    fun <T : AbstractWidget> add(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        weight: Int = 1,
    ) {
        add(widget, layoutSettings, weight) { w, width, height -> w.setSize(width, height) }
    }

    fun <T> add(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        weight: Int = 1,
    ) where T : ResizableLayout, T : LayoutElement {
        add(widget, layoutSettings, weight) { w, width, height -> w.setDimensions(width, height) }
    }

    override fun arrangeElements() {
        val count = elements.size
        if (count == 0) return

        when (direction) {
            Direction.HORIZONTAL -> refreshHorizontal()
            Direction.VERTICAL -> refreshVertical()
        }

        super.arrangeElements()
    }

    private fun refreshHorizontal() {
        val count = elements.size
        val horizontalPadding = padding.left + padding.right
        val availableWidth = width - horizontalPadding - gap * (count - 1)
        val availableHeight = height - padding.top - padding.bottom

        val totalWeight = elements.sumOf { it.weight }
        val baseSize = availableWidth / totalWeight

        var remainingSpace = availableWidth - baseSize * totalWeight
        var currentX = x + padding.left

        elements.forEach { element ->
            var elementWidth = baseSize * element.weight
            if (remainingSpace > 0) {
                elementWidth += 1
                remainingSpace -= 1
            }

            element.setSize(elementWidth, availableHeight)
            element.setX(currentX, currentX + elementWidth)
            element.setY(y + padding.top, y + height - padding.bottom)

            currentX += elementWidth + gap
        }
    }

    private fun refreshVertical() {
        val count = elements.size
        val verticalPadding = padding.top + padding.bottom
        val availableWidth = width - padding.left - padding.right
        val availableHeight = height - verticalPadding - gap * (count - 1)

        val totalWeight = elements.sumOf { it.weight }
        val baseSize = availableHeight / totalWeight

        var remainingSpace = availableHeight - baseSize * totalWeight
        var currentY = y + padding.top

        elements.forEach { element ->
            var elementHeight = baseSize * element.weight
            if (remainingSpace > 0) {
                elementHeight += 1
                remainingSpace -= 1
            }

            element.setSize(availableWidth, elementHeight)
            element.setX(x + padding.left, x + width - padding.right)
            element.setY(currentY, currentY + elementHeight)

            currentY += elementHeight + gap
        }
    }
}
