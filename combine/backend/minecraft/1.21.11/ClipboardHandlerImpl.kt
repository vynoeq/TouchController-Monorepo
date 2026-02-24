package top.fifthlight.combine.backend.minecraft_1_21_11

import net.minecraft.client.Minecraft
import top.fifthlight.combine.input.text.ClipboardHandler

object ClipboardHandlerImpl : ClipboardHandler {
    private val client by lazy { Minecraft.getInstance() }

    override var text: String
        get() = client.keyboardHandler.clipboard
        set(value) {
            client.keyboardHandler.clipboard = value
        }
}