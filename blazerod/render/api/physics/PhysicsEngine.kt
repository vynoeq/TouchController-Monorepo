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

    fun getWorld(instance: ModelInstance): PhysicsWorld? {
        return activeWorlds[instance]
    }

    fun update(time: Float) {
        val iterator = activeWorlds.iterator()
        while (iterator.hasNext()) {
            val (instance, world) = iterator.next()
            if (instance.referenceCount <= 0) {
                world.dispose()
                iterator.remove()
            }
        }
    }
}

interface PhysicsWorld {
    fun resetRigidBody(rigidBodyIndex: Int, position: org.joml.Vector3f, rotation: org.joml.Quaternionf)
    fun pullTransforms(dst: FloatArray)
    fun pushTransforms(src: FloatArray)
    fun step(deltaTime: Float, maxSubSteps: Int, fixedTimeStep: Float)
    fun dispose()
}

interface PhysicsProvider {
    fun createWorld(instance: ModelInstance): PhysicsWorld
}
