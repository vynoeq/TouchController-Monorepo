package top.fifthlight.touchcontroller.version_26_1.fabric.gal

import net.minecraft.client.KeyMapping
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.version_26_1.gal.AbstractKeyBindingHandlerImpl
import top.fifthlight.touchcontroller.version_26_1.mixin.KeyMappingAccessor

@ActualImpl(KeyBindingHandler::class)
object KeyBindingHandlerImpl : AbstractKeyBindingHandlerImpl() {
    @JvmStatic
    @ActualConstructor
    fun of(): KeyBindingHandler = this

    override fun getKeyBinding(name: String): KeyMapping? = KeyMapping.get(name)

    override fun getAllKeyBinding(): Map<String, KeyMapping> =
        KeyMappingAccessor.`touchcontroller$getAllKeyMappings`()
}
