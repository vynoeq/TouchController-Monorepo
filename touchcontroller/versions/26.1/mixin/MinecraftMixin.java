package top.fifthlight.touchcontroller.version_26_1.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.common.event.render.RenderEvents;
import top.fifthlight.touchcontroller.common.model.TouchControllerLoadStatus;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    public void onRenderStart(boolean tick, CallbackInfo ci) {
        var instance = TouchControllerLoadStatus.INSTANCE;
        if (instance.isLoaded()) {
            RenderEvents.onRenderStart();
        }
    }
}
