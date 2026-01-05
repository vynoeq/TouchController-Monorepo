package top.fifthlight.combine.backend.minecraft_1_21_8

import net.minecraft.resources.ResourceLocation
import top.fifthlight.combine.theme.vanilla.VanillaDrawableProvider
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(VanillaDrawableProvider::class)
object VanillaDrawableProviderImpl: VanillaDrawableProvider {
    @ActualConstructor
    @JvmStatic
    fun of() = this

    override val buttonDrawableSet = DrawableSet(
        normal = TextureImpl(
            resourceLocation = ResourceLocation.withDefaultNamespace("widget/button"),
            size = IntSize(150, 20),
            padding = IntPadding(2),
        ),
        focus = TextureImpl(
            resourceLocation = ResourceLocation.withDefaultNamespace("widget/button_highlighted"),
            size = IntSize(150, 20),
            padding = IntPadding(2),
        ),
        disabled = TextureImpl(
            resourceLocation = ResourceLocation.withDefaultNamespace("widget/button_disabled"),
            size = IntSize(150, 20),
            padding = IntPadding(2),
        ),
    )
}
