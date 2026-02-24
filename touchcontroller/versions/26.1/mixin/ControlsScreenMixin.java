package top.fifthlight.touchcontroller.version_26_1.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.combine.backend.minecraft_26_1.TextImpl;
import top.fifthlight.touchcontroller.common.ui.config.screen.ConfigScreenKt;

@Mixin(ControlsScreen.class)
public abstract class ControlsScreenMixin {
    @Inject(at = @At("TAIL"), method = "addOptions")
    protected void addOptions(CallbackInfo ci) {
        var client = Minecraft.getInstance();
        var screen = (ControlsScreen) (Object) this;
        var body = ((OptionsSubScreenAccessor) this).body();
        var text = ConfigScreenKt.getConfigScreenButtonText();
        var component = ((TextImpl) text).getInner();
        body.addSmall(
                Button.builder(
                        component,
                        btn -> client.setScreen((Screen) ConfigScreenKt.getConfigScreen(screen))
                ).build(), null
        );
    }
}
