package top.fifthlight.touchcontroller.common.ui.component.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentType
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class ComponentScreenModel(
    initialValue: PersistentList<ItemDataComponentType>,
    private val onValueChanged: (PersistentList<ItemDataComponentType>) -> Unit,
) : TouchControllerScreenModel() {
    private val _value = MutableStateFlow(initialValue)
    val value = _value.asStateFlow()

    fun addItem(item: ItemDataComponentType) {
        val newValue = _value.updateAndGet {
            if (item !in it) {
                it + item
            } else {
                it
            }
        }
        onValueChanged(newValue)
    }

    fun removeItem(index: Int) {
        val newValue = _value.updateAndGet { it.removeAt(index) }
        onValueChanged(newValue)
    }
}
