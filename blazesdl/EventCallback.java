package top.fifthlight.blazesdl;

import org.lwjgl.glfw.*;

public class EventCallback {
    private EventCallback() {
    }

    public static GLFWFramebufferSizeCallbackI onFramebufferResize;
    public static GLFWWindowPosCallbackI onWindowMove;
    public static GLFWWindowSizeCallbackI onWindowResize;
    public static GLFWWindowFocusCallbackI onWindowFocus;
    public static GLFWCursorEnterCallbackI onWindowCursorEnter;
    public static GLFWWindowIconifyCallbackI onWindowIconify;

    public static GLFWKeyCallbackI keyPressCallback;
    public static GLFWCharCallbackI charTypedCallback;
    public static GLFWPreeditCallbackI preeditCallback;

    public static GLFWCursorPosCallbackI onMoveCallback;
    public static GLFWMouseButtonCallbackI onPressCallback;
    public static GLFWScrollCallbackI onScrollCallback;
    public static GLFWDropCallbackI onDropCallback;
}
