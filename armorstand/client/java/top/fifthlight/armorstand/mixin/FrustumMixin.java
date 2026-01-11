package top.fifthlight.armorstand.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.armorstand.PlayerRenderer;
import top.fifthlight.blazerod.api.resource.CameraTransform;

// TODO: modify Frustum to match real camera
@Mixin(Frustum.class)
public abstract class FrustumMixin {
    @Inject(method = "offsetToFullyIncludeCameraCube", at = @At("HEAD"), cancellable = true)
    public void wrapCoverBoxAroundSetPosition(int boxSize, CallbackInfoReturnable<Frustum> cir) {
        var transform = PlayerRenderer.getCurrentCameraTransform();
        if (transform == null) {
            return;
        }
        if (transform instanceof CameraTransform.Orthographic) {
            cir.setReturnValue((Frustum) (Object) this);
        }
    }

    @Inject(method = "cubeInFrustum(DDDDDD)I", at = @At("HEAD"))
    public void wrapintersectAab(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, CallbackInfoReturnable<Integer> cir) {
        var transform = PlayerRenderer.getCurrentCameraTransform();
        if (transform == null) {
            return;
        }
        if (transform instanceof CameraTransform.Orthographic orthographic) {
            // TODO mixin for frustum
        }
    }
}
