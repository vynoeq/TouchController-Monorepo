package top.fifthlight.touchcontroller.version_26_1.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.fifthlight.touchcontroller.version_26_1.extensions.ChatComponentWithMessages;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements ChatComponentWithMessages {
    @Shadow
    @Final
    private List<GuiMessage> allMessages;

    @Override
    public List<GuiMessage> touchcontroller$getMessages() {
        return allMessages;
    }
}
