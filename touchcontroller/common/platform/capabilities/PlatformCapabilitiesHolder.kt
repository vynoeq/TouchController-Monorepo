package top.fifthlight.touchcontroller.common.platform.capabilities

import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.fifthlight.touchcontroller.proxy.client.PlatformCapability

object PlatformCapabilitiesHolder {
    private val _platformCapabilities = MutableStateFlow(persistentSetOf<PlatformCapability>())
    val platformCapabilities = _platformCapabilities.asStateFlow()

    fun addCapability(capability: PlatformCapability) {
        _platformCapabilities.value += capability
    }

    fun removeCapability(capability: PlatformCapability) {
        _platformCapabilities.value -= capability
    }
}
