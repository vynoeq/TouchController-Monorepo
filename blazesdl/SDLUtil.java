package top.fifthlight.blazesdl;

import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLVideo;

import java.nio.ByteBuffer;

public class SDLUtil {
    public static final boolean IS_WAYLAND = "wayland".equalsIgnoreCase(SDLVideo.SDL_GetCurrentVideoDriver());

    public static @Nullable ByteBuffer keyboardState;

    public static boolean isMouseGrabbed = false;
    public static float virtualMouseX = 0f;
    public static float virtualMouseY = 0f;
    public static float realMouseX = 0f;
    public static float realMouseY = 0f;
}
