package top.fifthlight.combine.backend.minecraft_26_1

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import top.fifthlight.combine.data.LocalTextFactory
import top.fifthlight.combine.input.pointer.PointerButton
import top.fifthlight.combine.input.pointer.PointerEvent
import top.fifthlight.combine.input.pointer.PointerEventType
import top.fifthlight.combine.input.pointer.PointerType
import top.fifthlight.combine.input.text.LocalClipboard
import top.fifthlight.combine.node.CombineOwner
import top.fifthlight.combine.screen.LocalOnDismissRequestDispatcher
import top.fifthlight.combine.screen.LocalScreenFactory
import top.fifthlight.combine.screen.OnDismissRequestDispatcher
import top.fifthlight.combine.screen.ScreenFactory
import top.fifthlight.combine.sound.LocalSoundManager
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.combine.data.Text as CombineText
import top.fifthlight.combine.input.key.KeyEvent as CombineKeyEvent

class CombineScreen(
    title: Component,
    private val renderBackground: Boolean,
    private val parent: Screen?,
) : Screen(title) {
    private val client: Minecraft = Minecraft.getInstance()
    private var initialized = false
    private val dismissDispatcher = OnDismissRequestDispatcher()
    private val soundManager = SoundManagerImpl(client.soundManager)
    private val owner = CombineOwner(dispatcher = GameDispatcher, textMeasurer = TextMeasurerImpl)

    fun setContent(content: @Composable () -> Unit) {
        owner.setContent {
            CompositionLocalProvider(
                LocalSoundManager provides soundManager,
                LocalTextFactory provides TextFactoryImpl,
                LocalClipboard provides ClipboardHandlerImpl,
                LocalScreenFactory provides ScreenFactoryImpl,
                LocalOnDismissRequestDispatcher provides dismissDispatcher,
            ) {
                content()
            }
        }
    }

    override fun init() {
        super.init()
        if (!initialized) {
            initialized = true
            owner.start()
        }
    }

    private fun mapMouseButton(button: Int) = when (button) {
        0 -> PointerButton.Left
        1 -> PointerButton.Middle
        2 -> PointerButton.Right
        else -> null
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseButton = mapMouseButton(event.button()) ?: return true
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = event.x.toFloat(),
                    y = event.y.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = mouseButton,
                type = PointerEventType.Press,
            ),
        )
        return true
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        val mouseButton = mapMouseButton(event.button()) ?: return true
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = event.x.toFloat(),
                    y = event.y.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = mouseButton,
                type = PointerEventType.Release,
            ),
        )
        return true
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = mouseX.toFloat(),
                    y = mouseY.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = null,
                type = PointerEventType.Move,
            ),
        )
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double,
    ): Boolean {
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = mouseX.toFloat(),
                    y = mouseY.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = null,
                scrollDelta = Offset(
                    x = horizontalAmount.toFloat(),
                    y = verticalAmount.toFloat(),
                ),
                type = PointerEventType.Scroll,
            ),
        )
        return true
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        owner.onTextInput(event.codepointAsString())
        return true
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key == GLFW.GLFW_KEY_ESCAPE) {
            if (dismissDispatcher.hasEnabledCallbacks()) {
                dismissDispatcher.dispatchOnDismissed()
            } else {
                onClose()
            }
            return true
        }
        owner.onKeyEvent(
            CombineKeyEvent(
                key = mapKeyCode(event.key),
                modifier = mapModifier(event.modifiers),
                pressed = true,
            ),
        )
        return true
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        owner.onKeyEvent(
            CombineKeyEvent(
                key = mapKeyCode(event.key),
                modifier = mapModifier(event.modifiers),
                pressed = false,
            ),
        )
        return true
    }

    override fun insertText(string: String, override: Boolean) {
        owner.onTextInput(string)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (renderBackground) {
            super.render(guiGraphics, mouseX, mouseY, delta)
        }

        owner.render(
            size = IntSize(width, height),
            cursorPos = Offset(mouseX.toFloat(), mouseY.toFloat()),
            canvas = CanvasImpl(guiGraphics),
        )
    }

    override fun onClose() {
        owner.close()
        client?.setScreen(parent)
    }
}

@ActualImpl(ScreenFactory::class)
object ScreenFactoryImpl : ScreenFactory {
    @ActualConstructor
    @JvmStatic
    fun of() = this

    override fun openScreen(
        renderBackground: Boolean,
        title: CombineText,
        content: @Composable () -> Unit,
    ) {
        val client = Minecraft.getInstance()
        val screen = getScreen(client.screen, renderBackground, title, content)
        client.setScreen(screen as Screen)
    }

    override fun getScreen(
        parent: Any?,
        renderBackground: Boolean,
        title: CombineText,
        content: @Composable () -> Unit,
    ): Any {
        val screen = CombineScreen(
            title.toMinecraft(),
            renderBackground = renderBackground,
            parent?.let { it as Screen },
        )
        screen.setContent {
            content()
        }
        return screen
    }
}
