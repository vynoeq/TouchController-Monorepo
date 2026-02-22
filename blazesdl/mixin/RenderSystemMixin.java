package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazesdl.*;
import top.fifthlight.blazesdl.SDLError;

import java.util.function.LongSupplier;

import static java.awt.SystemColor.window;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Redirect(method = "initBackendSystem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GLX;_initGlfw()Ljava/util/function/LongSupplier;"))
    private static LongSupplier redirectInitGlfw() {
        if (SDLInit.SDL_WasInit(SDLInit.SDL_INIT_VIDEO) == 0) {
            SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO);
        }

        final var freq = SDLTimer.SDL_GetPerformanceFrequency();
        var multiplier = 1_000_000_000.0 / freq;
        return () -> (long) (SDLTimer.SDL_GetPerformanceCounter() * multiplier);
    }

    @Inject(method = "setErrorCallback", at = @At(value = "HEAD"), cancellable = true)
    private static void skipGlfwErrorCallback(GLFWErrorCallbackI onFullscreenError, CallbackInfo ci) {
        if (Minecraft.getInstance().getWindow() instanceof SDLWindow) {
            ci.cancel();
        }
    }

    @Redirect(method = "limitDisplayFPS", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetTime()D"))
    private static double replaceLimitFPSGetTime() {
        return SDLTimer.SDL_GetTicks() / 1000.0;
    }

    @Redirect(method = "limitDisplayFPS", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWaitEventsTimeout(D)V"))
    private static void redirectWaitEvents(double timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            SDLEvents.SDL_PumpEvents();
            return;
        }

        var timeoutMillis = (int) (timeoutSeconds * 1000.0);
        SDLEvents.SDL_WaitEventTimeout(null, timeoutMillis);
    }

    @Unique
    private static long windowIdToHandle(int id) {
        return SDLVideo.SDL_GetWindowFromID(id);
    }

    @Redirect(method = "pollEvents", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwPollEvents()V"))
    private static void overridePollEvents() {
        var minecraft = Minecraft.getInstance();
        if (!(minecraft.getWindow() instanceof SDLWindow sdlWindow)) {
            GLFW.glfwPollEvents();
            return;
        }

        SDLUtil.keyboardState = SDLKeyboard.SDL_GetKeyboardState();

        try (var stack = MemoryStack.stackPush()) {
            var event = SDL_Event.malloc(stack);
            while (SDLEvents.SDL_PollEvent(event)) {
                var eventType = event.type();
                switch (eventType) {
                    case SDLEvents.SDL_EVENT_DISPLAY_ADDED, SDLEvents.SDL_EVENT_DISPLAY_REMOVED ->
                            sdlWindow.getScreenManager().onMonitorChange(event.display().displayID(), eventType);

                    case SDLEvents.SDL_EVENT_WINDOW_MOVED -> {
                        var callback = EventCallback.onWindowMove;
                        if (callback != null) {
                            callback.invoke(windowIdToHandle(event.window().windowID()), event.window().data1(), event.window().data2());
                        }
                    }
                    case SDLEvents.SDL_EVENT_WINDOW_RESIZED -> {
                        var windowHandle = windowIdToHandle(event.window().windowID());
                        var windowCallback = EventCallback.onWindowResize;
                        if (windowCallback != null) {
                            windowCallback.invoke(windowHandle, event.window().data1(), event.window().data2());
                        }
                        var framebufferCallback = EventCallback.onFramebufferResize;
                        if (framebufferCallback != null) {
                            var w = stack.ints(0);
                            var h = stack.ints(0);

                            if (!SDLVideo.SDL_GetWindowSizeInPixels(windowHandle, w, h)) {
                                throw SDLError.handleError("SDL_GetWindowSizeInPixels");
                            }
                            framebufferCallback.invoke(windowHandle, w.get(), h.get());
                        }
                    }
                    case SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED, SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST -> {
                        var callback = EventCallback.onWindowFocus;
                        if (callback != null) {
                            callback.invoke(windowIdToHandle(event.window().windowID()), eventType == SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED);
                        }
                    }
                    case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED -> sdlWindow.shouldClose = true;

                    case SDLEvents.SDL_EVENT_KEY_DOWN, SDLEvents.SDL_EVENT_KEY_UP -> {
                        var callback = EventCallback.keyPressCallback;
                        if (callback != null) {
                            var key = event.key();
                            int action;
                            if (eventType == SDLEvents.SDL_EVENT_KEY_DOWN) {
                                if (key.repeat()) {
                                    action = GLFW.GLFW_REPEAT;
                                } else {
                                    action = GLFW.GLFW_PRESS;
                                }
                            } else {
                                action = GLFW.GLFW_RELEASE;
                            }
                            var keyCode = SDLKeyMapping.toGlfwKey(key.key());
                            var modifier = SDLKeyMapping.getGlfwModifiers(key.mod());
                            callback.invoke(windowIdToHandle(event.window().windowID()), keyCode, key.scancode(), action, modifier);
                        }
                    }
                    case SDLEvents.SDL_EVENT_TEXT_INPUT -> {
                        var callback = EventCallback.charTypedCallback;
                        if (callback != null) {
                            var text = event.text();
                            var windowHandle = windowIdToHandle(event.window().windowID());
                            var textStr = text.textString();
                            if (textStr != null) {
                                textStr.chars().forEach(ch -> callback.invoke(windowHandle, ch));
                            }
                        }
                    }
                    case SDLEvents.SDL_EVENT_TEXT_EDITING -> {
                        var callback = EventCallback.preeditCallback;
                        if (callback != null) {
                            var text = event.edit();
                            // TODO
                        }
                    }

                    case SDLEvents.SDL_EVENT_MOUSE_MOTION -> {
                        var callback = EventCallback.onMoveCallback;
                        if (callback != null) {
                            var motion = event.motion();
                            SDLUtil.realMouseX = motion.x();
                            SDLUtil.realMouseY = motion.y();
                            if (SDLUtil.isMouseGrabbed) {
                                SDLUtil.virtualMouseX += motion.xrel();
                                SDLUtil.virtualMouseY += motion.yrel();
                                callback.invoke(windowIdToHandle(motion.windowID()), SDLUtil.virtualMouseX, SDLUtil.virtualMouseY);
                            } else {
                                callback.invoke(windowIdToHandle(motion.windowID()), SDLUtil.realMouseX, SDLUtil.realMouseY);
                            }
                        }
                    }

                    case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN, SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> {
                        var callback = EventCallback.onPressCallback;
                        if (callback != null) {
                            var action = (eventType == SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN) ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE;
                            var buttonEvent = event.button();
                            int sdlButton = buttonEvent.button();
                            var button = switch (sdlButton) {
                                case SDLMouse.SDL_BUTTON_LEFT -> GLFW.GLFW_MOUSE_BUTTON_LEFT;
                                case SDLMouse.SDL_BUTTON_MIDDLE -> GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
                                case SDLMouse.SDL_BUTTON_RIGHT -> GLFW.GLFW_MOUSE_BUTTON_RIGHT;
                                default -> sdlButton - 1;
                            };
                            var modifier = SDLKeyMapping.getGlfwModifiers(SDLKeyboard.SDL_GetModState());
                            callback.invoke(windowIdToHandle(buttonEvent.windowID()), button, action, modifier);
                        }
                    }

                    case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> {
                        var callback = EventCallback.onScrollCallback;
                        if (callback != null) {
                            callback.invoke(windowIdToHandle(event.button().windowID()), event.wheel().x(), event.wheel().y());
                        }
                    }
                }
            }
        }
    }
}
