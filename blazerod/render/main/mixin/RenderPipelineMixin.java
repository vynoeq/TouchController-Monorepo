package top.fifthlight.blazerod.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import top.fifthlight.blazerod.extension.internal.RenderPipelineExtInternal;

import java.util.Set;

@Mixin(RenderPipeline.class)
public abstract class RenderPipelineMixin implements RenderPipelineExtInternal {
    @Unique
    private Set<String> blazerod$storageBuffers;

    @Override
    @NotNull
    public Set<String> blazerod$getStorageBuffers() {
        return blazerod$storageBuffers;
    }

    @Override
    public void blazerod$setStorageBuffers(@NotNull Set<String> storageBuffers) {
        this.blazerod$storageBuffers = storageBuffers;
    }
}
