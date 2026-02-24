package top.fifthlight.touchcontroller.common.ui.item.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class ItemListScreenModel(
    initialValue: PersistentList<Item>,
    private val onValueChanged: (PersistentList<Item>) -> Unit,
) : TouchControllerScreenModel() {
    private val _value = MutableStateFlow(initialValue)
    val value = _value.asStateFlow()

    fun addItem(item: Item) {
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
