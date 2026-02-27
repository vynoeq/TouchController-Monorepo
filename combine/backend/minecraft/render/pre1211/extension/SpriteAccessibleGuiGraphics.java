package top.fifthlight.combine.backend.minecraft.render.pre1211.extension;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public interface SpriteAccessibleGuiGraphics {
    TextureAtlasSprite combine$getSprite(ResourceLocation resourceLocation);
}
