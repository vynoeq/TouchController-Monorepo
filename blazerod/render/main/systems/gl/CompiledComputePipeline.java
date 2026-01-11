package top.fifthlight.blazerod.systems.gl;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import top.fifthlight.blazerod.systems.ComputePipeline;

public record CompiledComputePipeline(ComputePipeline info, GlProgram program) implements CompiledRenderPipeline {
    @Override
    public boolean isValid() {
        return this.program != GlProgram.INVALID_PROGRAM;
    }
}
