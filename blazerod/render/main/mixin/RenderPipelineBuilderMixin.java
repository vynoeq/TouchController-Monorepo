package top.fifthlight.blazerod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazerod.extension.internal.RenderPipelineBuilderExtInternal;
import top.fifthlight.blazerod.extension.internal.RenderPipelineExtInternal;
import top.fifthlight.blazerod.extension.internal.RenderPipelineSnippetExtInternal;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(RenderPipeline.Builder.class)
public abstract class RenderPipelineBuilderMixin implements RenderPipelineBuilderExtInternal {
    @Shadow
    private Optional<VertexFormat> vertexFormat;

    @Override
    public void blazerod$withVertexFormat(@NotNull VertexFormat format) {
        this.vertexFormat = Optional.of(format);
    }

    // Suppress: the field is actually initialized in the constructor by mixin
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Unique
    @NotNull
    Set<String> blazerod$storageBuffers;

    @Override
    @NotNull
    public Set<String> blazerod$getStorageBuffers() {
        return blazerod$storageBuffers;
    }

    @Override
    public void blazerod$withStorageBuffer(@NotNull String name) {
        blazerod$storageBuffers.add(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        blazerod$storageBuffers = new HashSet<>();
    }

    @Inject(method = "withSnippet", at = @At("HEAD"))
    void withSnippet(@NotNull RenderPipeline.Snippet snippet, CallbackInfo ci) {
        var snippetInternal = ((RenderPipelineSnippetExtInternal) (Object) snippet);
        this.blazerod$storageBuffers.addAll(snippetInternal.blazerod$getStorageBuffers());
    }

    @ModifyReturnValue(method = "buildSnippet", at = @At("RETURN"))
    public RenderPipeline.Snippet buildSnippet(@NotNull RenderPipeline.Snippet original) {
        var snippetExt = ((RenderPipelineSnippetExtInternal) (Object) original);
        snippetExt.blazerod$setStorageBuffers(blazerod$storageBuffers);
        return original;
    }

    @ModifyExpressionValue(
            method = "build",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Optional;isEmpty()Z",
                    slice = "builder"
            ),
            slice = {
                    @Slice(
                            id = "builder",
                            from = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;vertexFormatMode:Ljava/util/Optional;", opcode = Opcodes.GETFIELD),
                            to = @At(value = "NEW", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline;")
                    )
            }
    )
    public boolean isVertexModeEmpty(boolean original) {
        return false;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Redirect(
            method = "build",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Optional;get()Ljava/lang/Object;"
            ),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;vertexFormat:Ljava/util/Optional;", ordinal = 1, opcode = Opcodes.GETFIELD),
                    to = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;depthBiasScaleFactor:F", opcode = Opcodes.GETFIELD)
            )
    )
    public <T> T getVertexFormat(Optional<T> instance) {
        return instance.orElse(null);
    }

    @ModifyReturnValue(method = "build", at = @At("RETURN"))
    public RenderPipeline afterBuilt(RenderPipeline original) {
        var pipelineExt = ((RenderPipelineExtInternal) original);
        pipelineExt.blazerod$setStorageBuffers(blazerod$storageBuffers);
        return original;
    }
}
