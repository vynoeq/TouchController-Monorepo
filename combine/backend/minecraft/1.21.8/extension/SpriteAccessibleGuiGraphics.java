package top.fifthlight.combine.backend.minecraft_1_21_8.extension;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public interface SpriteAccessibleGuiGraphics {
    TextureAtlasSprite combine$getSprite(ResourceLocation resourceLocation);
}
