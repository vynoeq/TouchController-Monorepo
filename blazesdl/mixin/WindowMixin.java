package top.fifthlight.blazesdl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.*;
import org.lwjgl.glfw.*;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLPlatform;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.blazesdl.*;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Unique
    private SDLWindow blazesdl$getSdlWindow() {
        if ((Object) this instanceof SDLWindow window) {
            return window;
        } else {
            return null;
        }
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/platform/ScreenManager;"))
    private ScreenManager wrapScreenManager(MonitorCreator monitorCreator, Operation<ScreenManager> original) {
        if (blazesdl$getSdlWindow() != null) {
            return new SDLScreenManager(SDLMonitor::new);
        }
        return original.call(monitorCreator);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetPrimaryMonitor()J"))
    private long wrapGetPrimaryMonitor(Operation<Long> original) {
        if (blazesdl$getSdlWindow() != null) {
            return SDLVideo.SDL_GetPrimaryDisplay();
        }
        return original.call();
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetWindowPos(J[I[I)V"))
    private void wrapGlfwGetWindowPos(long window, int[] xpos, int[] ypos, Operation<Void> original) {
        if (blazesdl$getSdlWindow() == null) {
            original.call(window, xpos, ypos);
            return;
        }
        try (var stack = MemoryStack.stackPush()) {
            var x = stack.ints(-1);
            var y = stack.ints(-1);
            if (!SDLVideo.SDL_GetWindowPosition(window, x, y)) {
                throw SDLError.handleError("SDL_GetWindowPosition");
            }
            xpos[0] = x.get();
            ypos[0] = y.get();
        }
    }


    @WrapOperation(method = "refreshFramebufferSize", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetFramebufferSize(J[I[I)V"))
    private void wrapGlfwGetFramebufferSize(long window, int[] width, int[] height, Operation<Void> original) {
        if (blazesdl$getSdlWindow() == null) {
            original.call(window, width, height);
            return;
        }
        try (var stack = MemoryStack.stackPush()) {
            var w = stack.ints(-1);
            var h = stack.ints(-1);
            if (!SDLVideo.SDL_GetWindowSizeInPixels(window, w, h)) {
                throw SDLError.handleError("SDL_GetWindowSizeInPixels");
            }
            width[0] = w.get();
            height[0] = h.get();
        }
    }

    @WrapOperation(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwDestroyWindow(J)V"))
    private void wrapGlfwDestroyWindow(long window, Operation<Void> original) {
        if (blazesdl$getSdlWindow() == null) {
            original.call(window);
            return;
        }
        SDLVideo.SDL_DestroyWindow(window);
    }

    @WrapOperation(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwTerminate()V"))
    private void wrapGlfwTerminate(Operation<Void> original) {
        if (blazesdl$getSdlWindow() == null) {
            original.call();
            return;
        }
        SDLInit.SDL_Quit();
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetFramebufferSizeCallback(JLorg/lwjgl/glfw/GLFWFramebufferSizeCallbackI;)Lorg/lwjgl/glfw/GLFWFramebufferSizeCallback;"))
    private GLFWFramebufferSizeCallback cancelSetFramebufferSizeCallback(long window, GLFWFramebufferSizeCallbackI cbfun, Operation<GLFWFramebufferSizeCallback> original) {
        if (blazesdl$getSdlWindow() != null) {
            EventCallback.onFramebufferResize = cbfun;
            return null;
        }
        return original.call(window, cbfun);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowPosCallback(JLorg/lwjgl/glfw/GLFWWindowPosCallbackI;)Lorg/lwjgl/glfw/GLFWWindowPosCallback;"))
    private GLFWWindowPosCallback cancelSetWindowPosCallback(long window, GLFWWindowPosCallbackI cbfun, Operation<GLFWWindowPosCallback> original) {
        if (blazesdl$getSdlWindow() != null) {
            EventCallback.onWindowMove = cbfun;
            return null;
        }
        return original.call(window, cbfun);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowSizeCallback(JLorg/lwjgl/glfw/GLFWWindowSizeCallbackI;)Lorg/lwjgl/glfw/GLFWWindowSizeCallback;"))
    private GLFWWindowSizeCallback cancelSetWindowSizeCallback(long window, GLFWWindowSizeCallbackI cbfun, Operation<GLFWWindowSizeCallback> original) {
        if (blazesdl$getSdlWindow() != null) {
            EventCallback.onWindowResize = cbfun;
            return null;
        }
        return original.call(window, cbfun);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowFocusCallback(JLorg/lwjgl/glfw/GLFWWindowFocusCallbackI;)Lorg/lwjgl/glfw/GLFWWindowFocusCallback;"))
    private GLFWWindowFocusCallback cancelSetWindowFocusCallback(long window, GLFWWindowFocusCallbackI cbfun, Operation<GLFWWindowFocusCallback> original) {
        if (blazesdl$getSdlWindow() != null) {
            EventCallback.onWindowFocus = cbfun;
            return null;
        }
        return original.call(window, cbfun);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursorEnterCallback(JLorg/lwjgl/glfw/GLFWCursorEnterCallbackI;)Lorg/lwjgl/glfw/GLFWCursorEnterCallback;"))
    private GLFWCursorEnterCallback cancelSetCursorEnterCallback(long window, GLFWCursorEnterCallbackI cbfun, Operation<GLFWCursorEnterCallback> original) {
        if (blazesdl$getSdlWindow() != null) {
            EventCallback.onWindowCursorEnter = cbfun;
            return null;
        }
        return original.call(window, cbfun);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowIconifyCallback(JLorg/lwjgl/glfw/GLFWWindowIconifyCallbackI;)Lorg/lwjgl/glfw/GLFWWindowIconifyCallback;"))
    private GLFWWindowIconifyCallback cancelSetWindowIconifyCallback(long window, GLFWWindowIconifyCallbackI cbfun, Operation<GLFWWindowIconifyCallback> original) {
        if (blazesdl$getSdlWindow() != null) {
            EventCallback.onWindowIconify = cbfun;
            return null;
        }
        return original.call(window, cbfun);
    }

    @Inject(method = "getPlatform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetPlatform()I"), cancellable = true)
    private static void overrideGetPlatform(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(SDLPlatform.SDL_GetPlatform());
    }
}
