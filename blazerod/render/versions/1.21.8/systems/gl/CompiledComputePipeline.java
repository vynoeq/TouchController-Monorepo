package top.fifthlight.blazerod.render.version_1_21_8.systems.gl;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import top.fifthlight.blazerod.render.version_1_21_8.systems.ComputePipeline;

public record CompiledComputePipeline(ComputePipeline info, GlProgram program) implements CompiledRenderPipeline {
    @Override
    public boolean isValid() {
        return this.program != GlProgram.INVALID_PROGRAM;
    }
}
