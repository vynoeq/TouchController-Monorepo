package top.fifthlight.touchcontroller.common.layout.data

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import top.fifthlight.touchcontroller.common.gal.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.gamestate.CameraPerspective
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget
import kotlin.uuid.Uuid

data class ContextInput(
    val inGui: Boolean = false,
    val builtInCondition: PersistentSet<BuiltinLayerConditionKey.Key> = persistentSetOf(),
    val customCondition: PersistentSet<Uuid> = persistentSetOf(),
    val playerHandle: PlayerHandle? = null,
    val crosshairTarget: CrosshairTarget? = null,
    val perspective: CameraPerspective = CameraPerspective.FIRST_PERSON,
) {
    companion object {
        val EMPTY = ContextInput()
    }
}
