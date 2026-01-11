package top.fifthlight.armorstand.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.armorstand.extension.internal.PlayerRenderStateExtInternal;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("HEAD"))
    public void onUpdateRenderState(AbstractClientPlayer entity, PlayerRenderState state, float tickProgress, CallbackInfo ci) {
        var stateInternal = ((PlayerRenderStateExtInternal) state);
        stateInternal.armorstand$setUuid(entity.getUUID());
        top.fifthlight.armorstand.PlayerRenderer.updatePlayer(entity, state);
    }
}
