package top.fifthlight.blazerod.render.version_1_21_8.api.animation

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.mergetools.api.ExpectFactory

interface AnimationContexts {
    fun base(): AnimationContext
    fun entity(entity: Entity): AnimationContext
    fun livingEntity(entity: LivingEntity): AnimationContext
    fun player(player: Player): AnimationContext

    @ExpectFactory
    interface Factory {
        fun create(): AnimationContexts
    }
}
