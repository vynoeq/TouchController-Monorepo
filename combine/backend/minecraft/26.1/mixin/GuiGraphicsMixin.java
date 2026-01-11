package top.fifthlight.combine.backend.minecraft_26_1.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.fifthlight.combine.backend.minecraft_26_1.extension.SpriteAccessibleGuiGraphics;
import top.fifthlight.combine.backend.minecraft_26_1.extension.SubmittableGuiGraphics;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements SubmittableGuiGraphics, SpriteAccessibleGuiGraphics {
    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Shadow
    @Final
    private GuiGraphics.ScissorStack scissorStack;

    @Shadow
    @Final
    private TextureAtlas guiSprites;

    @ModifyArg(method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/render/state/GuiRenderState;II)V",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;<init>(I)V", ordinal = 0))
    private static int modifyStackLimit(int stackSize) {
        return Math.max(stackSize, 64);
    }

    @Override
    public void combine$submitElement(GuiElementRenderState guiElementRenderState) {
        guiRenderState.submitGuiElement(guiElementRenderState);
    }

    @Override
    public ScreenRectangle combine$peekScissorStack() {
        return scissorStack.peek();
    }

    @Override
    public TextureAtlasSprite combine$getSprite(Identifier Identifier) {
        return guiSprites.getSprite(Identifier);
    }
}
