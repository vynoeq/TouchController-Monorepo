package top.fifthlight.touchcontroller.version_1_21_11.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;

@Mixin(MouseHandler.class)
abstract class DisableMouseDirectionMixin {
    @Inject(
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;",
                    ordinal = 1
            ),
            method = "turnPlayer",
            cancellable = true
    )
    private void turnPlayer(CallbackInfo ci) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        if (configHolder == null) {
            return;
        }
        var config = configHolder.getConfig().getValue();
        if (config.getRegular().getDisableMouseMove()) {
            ci.cancel();
        }
    }
}