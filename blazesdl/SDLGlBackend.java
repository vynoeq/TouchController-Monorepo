package top.fifthlight.blazesdl;

import com.mojang.blaze3d.opengl.GlBackend;
import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.WindowAndDevice;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLVideo;

public class SDLGlBackend extends GlBackend {
    public static BackendCreationException handleError(String func) {
        var error = SDLError.SDL_GetError();
        if (error != null) {
            return new BackendCreationException("Function " + func + " failed with cause: " + error);
        } else {
            return new BackendCreationException("Function " + func + " failed with no cause");
        }
    }

    private void setGlAttribute(int attr, int value) throws BackendCreationException {
        if (!SDLVideo.SDL_GL_SetAttribute(attr, value)) {
            throw handleError("SDL_GL_SetAttribute");
        }
    }

    @Override
    public @NonNull WindowAndDevice createDeviceWithWindow(int width, int height, @NonNull String title, long monitor, @NonNull ShaderSource defaultShaderSource, @NonNull GpuDebugOptions debugOptions) throws BackendCreationException {
        if (!SDLVideo.SDL_GL_LoadLibrary((String) null)) {
            throw handleError("SDL_GL_LoadLibrary");
        }
        GL.create(SDLVideo::SDL_GL_GetProcAddress);

        setGlAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, VERSION_MAJOR);
        setGlAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, VERSION_MINOR);
        setGlAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE);
        setGlAttribute(SDLVideo.SDL_GL_CONTEXT_FLAGS, SDLVideo.SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG);

        var window = SDLVideo.SDL_CreateWindow(title, width, height, SDLVideo.SDL_WINDOW_OPENGL | SDLVideo.SDL_WINDOW_RESIZABLE | SDLVideo.SDL_WINDOW_HIGH_PIXEL_DENSITY);
        if (window == 0L) {
            throw handleError("SDL_CreateWindow");
        }

        var context = SDLVideo.SDL_GL_CreateContext(window);
        if (context == 0L) {
            throw handleError("SDL_GL_CreateContext");
        }

        // We must call it before GlDevice constructor, because there is no way to pass context
        SDLVideo.SDL_GL_MakeCurrent(window, context);
        return new WindowAndDevice(window, new GpuDevice(new SDLGlDevice(window, context, defaultShaderSource, debugOptions)));
    }
}
