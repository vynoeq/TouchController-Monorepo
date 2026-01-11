package top.fifthlight.blazerod.mixin.gl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.blazerod.extension.ShaderTypeExt;
import top.fifthlight.blazerod.extension.internal.GpuBufferExtInternal;
import top.fifthlight.blazerod.extension.internal.RenderPipelineExtInternal;
import top.fifthlight.blazerod.extension.internal.gl.GpuDeviceExtInternal;
import top.fifthlight.blazerod.extension.internal.gl.ShaderProgramExtInternal;
import top.fifthlight.blazerod.render.gl.ShaderProgramExt;
import top.fifthlight.blazerod.systems.ComputePipeline;
import top.fifthlight.blazerod.systems.gl.CompiledComputePipeline;
import top.fifthlight.blazerod.util.glsl.GlslExtensionProcessor;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mixin(GlDevice.class)
public abstract class GlDeviceMixin implements GpuDeviceExtInternal {
    @Unique
    private static final boolean blazerod$allowGlTextureBufferRange = true;
    @Unique
    private static final boolean blazerod$allowGlShaderStorageBufferObject = true;
    @Unique
    private static final boolean blazerod$allowSsboInVertexShader = true;
    @Unique
    private static final boolean blazerod$allowSsboInFragmentShader = true;
    @Unique
    private static final boolean blazerod$allowGlComputeShader = true;
    @Unique
    private static final boolean blazerod$allowGlShaderImageLoadStore = true;
    @Unique
    private static final boolean blazerod$allowGlShaderPacking = true;

    @Shadow
    @Final
    private Set<String> enabledExtensions;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource;
    @Shadow
    @Final
    private GlDebugLabel debugLabels;

    @Unique
    private final Map<ComputePipeline, CompiledComputePipeline> blazerod$computePipelineCompileCache = new IdentityHashMap<>();
    @Unique
    private int blazerod$glMajorVersion;
    @Unique
    private int blazerod$glMinorVersion;

    @Unique
    private boolean blazerod$supportTextureBufferSlice;
    @Unique
    private boolean blazerod$supportSsbo;
    @Unique
    private boolean blazerod$supportComputeShader;
    @Unique
    private boolean blazerod$supportShaderImageLoadStore;
    @Unique
    private boolean blazerod$supportShaderPacking;
    @Unique
    private int blazerod$maxSsboBindings;
    @Unique
    private int blazerod$maxSsboInVertexShader;
    @Unique
    private int blazerod$maxSsboInFragmentShader;
    @Unique
    private int blazerod$ssboOffsetAlignment;
    @Unique
    private int blazerod$textureBufferOffsetAlignment;

    @Shadow
    public abstract GpuBuffer createBuffer(@Nullable Supplier<String> labelSupplier, int usage, int size);

    @Shadow
    public abstract GpuBuffer createBuffer(@Nullable Supplier<String> labelSupplier, int usage, ByteBuffer data);

    @Shadow
    protected abstract GlShaderModule getOrCompileShader(ResourceLocation id, ShaderType type, ShaderDefines defines, BiFunction<ResourceLocation, ShaderType, String> sourceRetriever);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(long contextId, int debugVerbosity, boolean sync, BiFunction<ResourceLocation, ShaderType, String> shaderSourceGetter, boolean debugLabels, CallbackInfo ci, @Local(ordinal = 0) GLCapabilities glCapabilities) {
        if (blazerod$allowGlTextureBufferRange && glCapabilities.GL_ARB_texture_buffer_range) {
            enabledExtensions.add("GL_ARB_texture_buffer_range");
            blazerod$supportTextureBufferSlice = true;
        } else {
            blazerod$supportTextureBufferSlice = false;
        }

        if (blazerod$allowGlShaderStorageBufferObject
                && glCapabilities.GL_ARB_shader_storage_buffer_object
                && glCapabilities.GL_ARB_program_interface_query
                // OpenGL spec says GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS must be at least 8, but we check it
                // just in case some drivers don't follow the spec
                && GL11.glGetInteger(GL43C.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS) > 8) {
            enabledExtensions.add("GL_ARB_shader_storage_buffer_object");
            enabledExtensions.add("GL_ARB_program_interface_query");
            blazerod$supportSsbo = true;
        } else {
            blazerod$supportSsbo = false;
        }

        if (blazerod$allowGlComputeShader && glCapabilities.GL_ARB_compute_shader) {
            enabledExtensions.add("GL_ARB_compute_shader");
            blazerod$supportComputeShader = true;
        } else {
            blazerod$supportComputeShader = false;
        }

        if (blazerod$allowGlShaderImageLoadStore && glCapabilities.GL_ARB_shader_image_load_store) {
            enabledExtensions.add("GL_ARB_shader_image_load_store");
            blazerod$supportShaderImageLoadStore = true;
        } else {
            blazerod$supportShaderImageLoadStore = false;
        }

        if (blazerod$allowGlShaderPacking && glCapabilities.GL_ARB_shading_language_packing) {
            enabledExtensions.add("GL_ARB_shading_language_packing");
            blazerod$supportShaderPacking = true;
        } else {
            blazerod$supportShaderPacking = false;
        }

        if (blazerod$supportSsbo && blazerod$allowSsboInVertexShader) {
            blazerod$maxSsboInVertexShader = GL11.glGetInteger(GL43C.GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS);
        } else {
            blazerod$maxSsboInVertexShader = 0;
        }
        if (blazerod$supportSsbo && blazerod$allowSsboInFragmentShader) {
            blazerod$maxSsboInFragmentShader = GL11.glGetInteger(GL43C.GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS);
        } else {
            blazerod$maxSsboInFragmentShader = 0;
        }
        if (blazerod$supportSsbo) {
            blazerod$maxSsboBindings = GL11.glGetInteger(GL43C.GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS);
        } else {
            blazerod$maxSsboBindings = 0;
        }

        if (blazerod$supportSsbo) {
            blazerod$ssboOffsetAlignment = GL11.glGetInteger(ARBShaderStorageBufferObject.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
        } else {
            blazerod$ssboOffsetAlignment = -1;
        }
        if (blazerod$supportTextureBufferSlice) {
            blazerod$textureBufferOffsetAlignment = GL11.glGetInteger(ARBTextureBufferRange.GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT);
        } else {
            blazerod$textureBufferOffsetAlignment = -1;
        }

        blazerod$glMajorVersion = GL11.glGetInteger(GL30C.GL_MAJOR_VERSION);
        blazerod$glMinorVersion = GL11.glGetInteger(GL30C.GL_MINOR_VERSION);
    }

    @Inject(method = "compilePipeline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlProgram;setupUniforms(Ljava/util/List;Ljava/util/List;)V"))
    private void onSetGlProgram(RenderPipeline pipeline, BiFunction<ResourceLocation, ShaderType, String> sourceRetriever, CallbackInfoReturnable<GlRenderPipeline> cir, @Local GlProgram shaderProgram) {
        var shaderProgramExt = (ShaderProgramExtInternal) shaderProgram;
        var pipelineExt = (RenderPipelineExtInternal) pipeline;
        shaderProgramExt.blazerod$setStorageBuffers(pipelineExt.blazerod$getStorageBuffers());
    }

    @WrapOperation(method = "compileShader(Lcom/mojang/blaze3d/opengl/GlDevice$ShaderCompilationKey;Ljava/util/function/BiFunction;)Lcom/mojang/blaze3d/opengl/GlShaderModule;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;injectDefines(Ljava/lang/String;Lnet/minecraft/client/renderer/ShaderDefines;)Ljava/lang/String;"))
    public String modifyShaderSource(String source, ShaderDefines defines, Operation<String> original) {
        var context = new GlslExtensionProcessor.Context(blazerod$glMajorVersion, blazerod$glMinorVersion, defines);
        var processedShader = GlslExtensionProcessor.process(context, source);
        return original.call(processedShader, defines);
    }

    @NotNull
    @Override
    public GpuBuffer blazerod$createBuffer(@Nullable Supplier<String> labelSupplier, int usage, int extraUsage, int size) {
        var buffer = createBuffer(labelSupplier, usage, size);
        ((GpuBufferExtInternal) buffer).blazerod$setExtraUsage(extraUsage);
        return buffer;
    }

    @NotNull
    @Override
    public GpuBuffer blazerod$createBuffer(@Nullable Supplier<String> labelSupplier, int usage, int extraUsage, ByteBuffer data) {
        var buffer = createBuffer(labelSupplier, usage, data);
        ((GpuBufferExtInternal) buffer).blazerod$setExtraUsage(extraUsage);
        return buffer;
    }

    @Override
    public boolean blazerod$supportTextureBufferSlice() {
        return blazerod$supportTextureBufferSlice;
    }

    @Override
    public boolean blazerod$supportSsbo() {
        return blazerod$supportSsbo;
    }

    @Override
    public boolean blazerod$supportComputeShader() {
        return blazerod$supportComputeShader;
    }

    @Override
    public boolean blazerod$supportMemoryBarrier() {
        // glMemoryBarrier is defined in ARB_shader_image_load_store
        return blazerod$supportShaderImageLoadStore;
    }

    @Override
    public boolean blazerod$supportShaderPacking() {
        return blazerod$supportShaderPacking;
    }

    @Override
    public int blazerod$getMaxSsboBindings() {
        if (!blazerod$supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return blazerod$maxSsboBindings;
    }

    @Override
    public int blazerod$getMaxSsboInVertexShader() {
        if (!blazerod$supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return blazerod$maxSsboInVertexShader;
    }

    @Override
    public int blazerod$getMaxSsboInFragmentShader() {
        if (!blazerod$supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return blazerod$maxSsboInFragmentShader;
    }

    @Override
    public int blazerod$getSsboOffsetAlignment() {
        if (!blazerod$supportSsbo) {
            throw new IllegalStateException("SSBO is not supported");
        }
        return blazerod$ssboOffsetAlignment;
    }

    @Override
    public int blazerod$getTextureBufferOffsetAlignment() {
        if (!blazerod$supportTextureBufferSlice) {
            throw new IllegalStateException("Texture buffer slice is not supported");
        }
        return blazerod$textureBufferOffsetAlignment;
    }

    @Override
    public CompiledComputePipeline blazerod$compilePipelineCached(ComputePipeline pipeline) {
        return this.blazerod$computePipelineCompileCache.computeIfAbsent(pipeline, p -> this.blazerod$compileComputePipeline(pipeline, defaultShaderSource));
    }

    @Unique
    private CompiledComputePipeline blazerod$compileComputePipeline(ComputePipeline pipeline, BiFunction<ResourceLocation, ShaderType, String> sourceRetriever) {
        var compiledShader = getOrCompileShader(pipeline.getComputeShader(), ShaderTypeExt.COMPUTE, pipeline.getShaderDefines(), sourceRetriever);
        if (compiledShader == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: compute shader {} was invalid", pipeline.getLocation(), pipeline.getComputeShader());
            return new CompiledComputePipeline(pipeline, GlProgram.INVALID_PROGRAM);
        } else {
            com.mojang.blaze3d.opengl.GlProgram shaderProgram;
            try {
                shaderProgram = ShaderProgramExt.create(compiledShader, pipeline.getLocation().toString());
            } catch (net.minecraft.client.renderer.ShaderManager.CompilationException ex) {
                LOGGER.error("Couldn't compile program for pipeline {}: {}", pipeline.getLocation(), ex);
                return new CompiledComputePipeline(pipeline, GlProgram.INVALID_PROGRAM);
            }

            shaderProgram.setupUniforms(pipeline.getUniforms(), pipeline.getSamplers());
            ((ShaderProgramExtInternal) shaderProgram).blazerod$setStorageBuffers(pipeline.getStorageBuffers());
            debugLabels.applyLabel(shaderProgram);
            return new CompiledComputePipeline(pipeline, shaderProgram);
        }
    }
}
