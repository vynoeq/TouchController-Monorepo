package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.AbstractLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import java.util.function.Consumer

class AutoHeightGridLayout(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    private val cellWidth: Int,
    private val cellHeightRange: IntRange,
    private val forceAtLeastOneRow: Boolean = true,
    private val verticalGap: Int = 0,
    private val padding: Insets = Insets.ZERO,
) : AbstractLayout(x, y, width, height), ResizableLayout {
    private class Element<T : LayoutElement>(
        private val inner: T,
        positioner: LayoutSettings,
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

    private var cellHeight = (cellHeightRange.first + cellHeightRange.last) / 2
    override fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
        val (_, contentHeight) = contentSize()
        val minRows =
            ((contentHeight + verticalGap) / (cellHeightRange.first + verticalGap)).coerceAtLeast(if (forceAtLeastOneRow) 1 else 0)
        val maxRows = ((contentHeight + verticalGap) / (cellHeightRange.last + verticalGap)).coerceAtLeast(minRows)
        val minRowsSpace = (contentHeight + verticalGap) - (minRows * (cellHeightRange.first + verticalGap))
        val maxRowsSpace = (contentHeight + verticalGap) - (maxRows * (cellHeightRange.last + verticalGap))
        val rows = if (minRowsSpace < maxRowsSpace) {
            minRows
        } else {
            maxRows
        }
        cellHeight = if (rows <= 0) {
            cellHeightRange.first
        } else {
            ((contentHeight - (rows - 1) * verticalGap) / rows).coerceIn(cellHeightRange)
        }
    }

    fun clear() = elements.clear()

    fun <T : LayoutElement> add(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        onSizeChanged: (widget: T, width: Int, height: Int) -> Unit,
    ) {
        elements.add(Element(widget, layoutSettings, onSizeChanged))
    }

    fun <T : AbstractWidget> add(widget: T, layoutSettings: LayoutSettings = LayoutSettings.defaults()) {
        add(widget, layoutSettings) { w, width, height -> w.setSize(width, height) }
    }

    fun <T> add(widget: T, layoutSettings: LayoutSettings = LayoutSettings.defaults())
            where T : ResizableLayout, T : LayoutElement {
        add(widget, layoutSettings) { w, width, height -> w.setDimensions(width, height) }
    }

    fun contentSize() = Pair(
        width - padding.left - padding.right,
        height - padding.top - padding.bottom,
    )

    /**
     * Calculate how many rows and columns of elements this grid can contain
     */
    fun calculateSize(): Pair<Int, Int> {
        val (availableWidth, availableHeight) = contentSize()
        val rows =
            ((availableHeight + verticalGap) / (cellHeight + verticalGap)).takeIf { !forceAtLeastOneRow || it >= 1 }
                ?: 1
        val columns = (availableWidth / cellWidth)
        return Pair(rows, columns)
    }

    override fun arrangeElements() {
        if (elements.isEmpty()) return

        val (availableWidth, availableHeight) = contentSize()
        val effectiveCellHeight = if (forceAtLeastOneRow) {
            cellHeight.coerceAtMost(availableHeight)
        } else {
            cellHeight
        }

        val maxItemsPerRow = (availableWidth / cellWidth).coerceAtLeast(1)
        val horizontalGap = if (maxItemsPerRow > 1) {
            (availableWidth - (maxItemsPerRow * cellWidth)) / (maxItemsPerRow - 1)
        } else {
            0
        }

        var currentX = x + padding.left
        var currentY = y + padding.top
        var currentRow = 0

        elements.forEach { element ->
            if (currentRow >= maxItemsPerRow) {
                currentX = x + padding.left
                currentY += effectiveCellHeight + verticalGap
                currentRow = 0
            }

            element.setSize(cellWidth, effectiveCellHeight)
            element.setX(currentX, currentX + cellWidth)
            element.setY(currentY, currentY + effectiveCellHeight)

            currentX += cellWidth + horizontalGap
            currentRow++
        }

        super.arrangeElements()
    }
}
