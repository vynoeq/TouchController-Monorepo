package top.fifthlight.touchcontroller.version_1_21_11.mixin;

import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionsSubScreen.class)
public interface OptionsSubScreenAccessor {
    @Accessor("list")
    OptionsList body();
}