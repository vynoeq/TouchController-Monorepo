package top.fifthlight.blazesdl.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazesdl.SDLUtil;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @Inject(method = "setFocused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;setFocused(Z)V"))
    private void focusedChanged(boolean focused, CallbackInfo ci) {
        SDLUtil.updateTextInputStatus(focused);
    }

    @Inject(method = "renderWidget", at = @At(value = "TAIL"))
    private void updateTextPos(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci, @Local(name = "cursorX") int cursorX) {
        var editBox = (EditBox) (Object) this;
        if (!editBox.isVisible()) {
            return;
        }
        if (!editBox.isFocused()) {
            return;
        }
        if (editBox.preeditOverlay == null) {
            var window = Minecraft.getInstance().getWindow();
            SDLUtil.updateTextInputAreaScaled(window, editBox.getX(), editBox.getY(), editBox.getWidth(), editBox.getHeight(), cursorX - editBox.getX());
        }
    }
}
