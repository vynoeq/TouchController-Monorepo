package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.opengl.GlBackend;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.GpuBackend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.lwjgl.system.Configuration;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazesdl.SDLGlBackend;
import top.fifthlight.blazesdl.SDLWindow;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "<init>", at = @At(value = "HEAD"))
    private static void useExplicitInit(GameConfig gameConfig, CallbackInfo ci) {
        Configuration.OPENGL_EXPLICIT_INIT.set(true);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/platform/Window;"))
    private Window createWindow(WindowEventHandler eventHandler,
                                DisplayData displayData,
                                String fullscreenVideoModeString,
                                String title,
                                GpuBackend[] backends,
                                ShaderSource defaultShaderSource,
                                GpuDebugOptions debugOptions) {
        return new SDLWindow(eventHandler, displayData, fullscreenVideoModeString, title, backends, defaultShaderSource, debugOptions);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/opengl/GlBackend;"))
    private GlBackend replaceGlBackend() {
        LOGGER.info("BlazeSDL: Replacing GlBackend to SDLGlBackend!");
        return new SDLGlBackend();
    }
}
