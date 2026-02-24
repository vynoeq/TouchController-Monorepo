package top.fifthlight.touchcontroller.version_1_21_11.mixin;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.fifthlight.touchcontroller.version_1_21_11.extensions.ChatComponentWithMessages;

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
