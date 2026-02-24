package top.fifthlight.touchcontroller.common.ui.importpreset.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class ImportPresetScreenModel(
    private val onPresetKeySelected: (BuiltinPresetKey) -> Unit,
) : TouchControllerScreenModel() {
    private val _key = MutableStateFlow(BuiltinPresetKey())
    val key = _key.asStateFlow()

    fun updateKey(key: BuiltinPresetKey) {
        _key.value = key
    }

    fun finish() {
        onPresetKeySelected(key.value)
    }
}
