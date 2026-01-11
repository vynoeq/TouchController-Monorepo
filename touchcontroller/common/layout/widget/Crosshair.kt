package top.fifthlight.touchcontroller.common.layout.widget

import top.fifthlight.combine.paint.*
import top.fifthlight.touchcontroller.common.gal.crosshair.CrosshairRenderer
import top.fifthlight.touchcontroller.common.gal.crosshair.CrosshairRendererFactory
import top.fifthlight.touchcontroller.common.layout.Context

fun Context.Crosshair() {
    val status = result.crosshairStatus ?: return
    val crosshairRenderer: CrosshairRenderer = CrosshairRendererFactory.of()

    val config = config.touchRing
    drawQueue.enqueue { canvas ->
        canvas.withTranslate(status.position * windowScaledSize) {
            if (status.breakPercent > 0f) {
                val progress = status.breakPercent * (1f - config.initialProgress) + config.initialProgress
                crosshairRenderer.renderInner(canvas, config, progress)
            }
            crosshairRenderer.renderOuter(canvas, config)
        }
    }
}