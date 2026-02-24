package top.fifthlight.touchcontroller.version_26_1.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;

@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
    @Shadow
    @Final
    public static int CURSOR_NORMAL;

    @Inject(at = @At("TAIL"), method = "grabOrReleaseMouse")
    private static void setCursorParameters(Window window, int cursorMode, double xpos, double ypos, CallbackInfo ci) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();
        if (config.getRegular().getDisableMouseLock()) {
            GLFW.glfwSetInputMode(window.handle(), GLFW_CURSOR, CURSOR_NORMAL);
        }
    }
}