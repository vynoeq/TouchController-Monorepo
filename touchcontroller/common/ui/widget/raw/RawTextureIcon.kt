package top.fifthlight.touchcontroller.common.ui.widget.raw

import androidx.compose.runtime.Composable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.data.IntSize
import top.fifthlight.mergetools.api.ExpectFactory

interface RawTextureIcon {
    @Composable
    fun Icon(
        modifier: Modifier = Modifier,
        identifier: Identifier,
        size: IntSize,
    )

    @ExpectFactory
    interface Factory {
        fun of(): RawTextureIcon
    }

    companion object : RawTextureIcon by RawTextureIconFactory.of() {
        @Composable
        operator fun invoke(
            modifier: Modifier = Modifier,
            identifier: Identifier,
            size: IntSize,
        ) = Icon(modifier, identifier, size)
    }
}
