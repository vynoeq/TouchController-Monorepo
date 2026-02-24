package top.fifthlight.touchcontroller.common.config.preset.builtin.key

import top.fifthlight.mergetools.api.ExpectFactory
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset

interface BuiltinPresetsProvider {
    fun generate(key: BuiltinPresetKey): LayoutPreset

    @ExpectFactory
    interface Factory {
        fun of(): BuiltinPresetsProvider
    }

    companion object : BuiltinPresetsProvider by BuiltinPresetsProviderFactory.of()
}
