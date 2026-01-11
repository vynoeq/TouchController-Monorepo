package top.fifthlight.blazerod.animation.context

import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import org.joml.Vector3d
import top.fifthlight.blazerod.mixin.MinecraftClientAccessor
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.util.*
import kotlin.jvm.optionals.getOrNull

open class BaseAnimationContext(
    protected val client: Minecraft = Minecraft.getInstance(),
) : AnimationContext {
    protected val tickCounter: DeltaTracker = client.deltaTracker
    protected val world: ClientLevel?
        get() = client.level

    override fun getGameTick(): Long = (client as MinecraftClientAccessor).clientTickCount

    override fun getDeltaTick(): Float = tickCounter.getGameTimeDeltaPartialTick(false)

    protected val vector3dBuffer = Vector3d()
    protected val intBuffer = MutableInt()
    protected val longBuffer = MutableLong()
    protected val floatBuffer = MutableFloat()
    protected val doubleBuffer = MutableDouble()
    protected val booleanBuffer = MutableBoolean()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getProperty(type: AnimationContext.Property<T>): T? = when (type) {
        AnimationContext.Property.GameTick -> longBuffer.apply { value = getGameTick() }

        AnimationContext.Property.DeltaTick -> floatBuffer.apply { value = getDeltaTick() }

        AnimationContext.Property.WorldTimeOfDay -> world?.let { world ->
            floatBuffer.apply {
                value = world.dayTime.toFloat() / 24000f
            }
        }

        AnimationContext.Property.WorldTimeStamp -> world?.let { world ->
            intBuffer.apply {
                value = world.dayTime.toInt()
            }
        }

        AnimationContext.Property.WorldMoonPhase -> world?.let { world ->
            intBuffer.apply {
                value = world.moonPhase
            }
        }

        AnimationContext.Property.WorldWeather -> world?.let { world ->
            intBuffer.apply {
                value = when {
                    !world.isRaining -> 0
                    world.isThundering -> 2
                    else -> 1
                }
            }
        }

        AnimationContext.Property.WorldDimension -> world
            ?.dimensionTypeRegistration()
            ?.unwrapKey()
            ?.getOrNull()
            ?.toString()

        AnimationContext.Property.GameFps -> intBuffer.apply {
            value = client.fps
        }

        else -> null
    } as T?

    companion object {
        @JvmStatic
        protected val propertyTypes = setOf(
            AnimationContext.Property.GameTick,
            AnimationContext.Property.DeltaTick,
            AnimationContext.Property.WorldTimeOfDay,
            AnimationContext.Property.WorldTimeStamp,
            AnimationContext.Property.WorldMoonPhase,
            AnimationContext.Property.WorldWeather,
            AnimationContext.Property.WorldDimension,
            AnimationContext.Property.GameFps,
        )
            @JvmName("getBasePropertyTypes")
            get

        val instance = BaseAnimationContext()
    }

    override fun getPropertyTypes(): Set<AnimationContext.Property<*>> = propertyTypes
}
