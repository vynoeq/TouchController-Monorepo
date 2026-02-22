package top.fifthlight.blazerod.api.physics

import top.fifthlight.blazerod.api.resource.ModelInstance

object PhysicsEngine {
    private val activeWorlds = mutableMapOf<ModelInstance, PhysicsWorld>()
    
    fun register(instance: ModelInstance, provider: PhysicsProvider) {
        if (!activeWorlds.containsKey(instance)) {
            activeWorlds[instance] = provider.createWorld(instance)
        }
    }

    fun unregister(instance: ModelInstance) {
        activeWorlds.remove(instance)?.dispose()
    }

    fun update(time: Float) {
        val iterator = activeWorlds.iterator()
        while (iterator.hasNext()) {
            val (instance, world) = iterator.next()
            if (instance.referenceCount <= 0) {
                world.dispose()
                iterator.remove()
            } else {
                world.update(time)
            }
        }
    }
}

interface PhysicsWorld {
    fun update(time: Float)
    fun dispose()
}

interface PhysicsProvider {
    fun createWorld(instance: ModelInstance): PhysicsWorld
}
