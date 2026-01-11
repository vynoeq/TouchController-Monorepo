package top.fifthlight.armorstand.mixin;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.armorstand.PlayerRenderer;
import top.fifthlight.armorstand.config.ConfigHolder;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private float partialTickTime;

    @ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    public float thirdPersonDistance(float f) {
        return f * ConfigHolder.INSTANCE.getConfig().getValue().getThirdPersonDistanceScale();
    }

    @Inject(method = "getXRot", at = @At("HEAD"), cancellable = true)
    public void wrapGetPitch(CallbackInfoReturnable<Float> cir) {
        var transform = PlayerRenderer.getCurrentCameraTransform();
        if (transform != null) {
            cir.setReturnValue(transform.getRotationEulerAngles().x());
        }
    }

    @Inject(method = "getYRot", at = @At("HEAD"), cancellable = true)
    public void wrapGetYaw(CallbackInfoReturnable<Float> cir) {
        var transform = PlayerRenderer.getCurrentCameraTransform();
        if (transform != null) {
            cir.setReturnValue(transform.getRotationEulerAngles().y());
        }
    }

    @Inject(method = "rotation", at = @At("HEAD"), cancellable = true)
    public void wrapGetRotation(CallbackInfoReturnable<Quaternionf> cir) {
        var transform = PlayerRenderer.getCurrentCameraTransform();
        if (transform != null) {
            cir.setReturnValue(transform.getRotationQuaternion());
        }
    }

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    public void wrapGetPos(CallbackInfoReturnable<Vec3> cir) {
        var transform = PlayerRenderer.getCurrentCameraTransform();
        if (transform != null) {
            var tickProgress = (double) this.partialTickTime;
            var position = transform.getPosition();
            cir.setReturnValue(new Vec3(
                    position.x() + Mth.lerp(tickProgress, entity.xo, entity.getX()),
                    position.y() + Mth.lerp(tickProgress, entity.yo, entity.getY()),
                    position.z() + Mth.lerp(tickProgress, entity.zo, entity.getZ())
            ));
        }
    }
}
