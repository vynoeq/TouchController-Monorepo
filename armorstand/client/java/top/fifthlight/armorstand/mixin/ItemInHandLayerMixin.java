package top.fifthlight.armorstand.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.armorstand.extension.internal.PlayerRenderStateExtInternal;
import top.fifthlight.armorstand.state.ModelInstanceManager;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @Inject(
        method = "render(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void armorstand$cancelVanillaHeldItemRender(
        ArmedEntityRenderState arg,
        PoseStack arg2,
        MultiBufferSource arg3,
        int i,
        CallbackInfo ci
    ) {
        if (!(arg instanceof PlayerRenderState)) {
            return;
        }

        var uuid = ((PlayerRenderStateExtInternal) arg).armorstand$getUuid();
        if (uuid == null) {
            return;
        }

        var entry = ModelInstanceManager.INSTANCE.get(uuid, System.nanoTime(), false);
        if (entry instanceof ModelInstanceManager.ModelInstanceItem.Model) {
            ci.cancel();
        }
    }
}
