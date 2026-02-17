package top.fifthlight.blazerod.render.version_1_21_8.extension.internal.gl;

import top.fifthlight.blazerod.render.version_1_21_8.extension.GpuDeviceExt;
import top.fifthlight.blazerod.render.version_1_21_8.systems.ComputePipeline;
import top.fifthlight.blazerod.render.version_1_21_8.systems.gl.CompiledComputePipeline;

public interface GpuDeviceExtInternal extends GpuDeviceExt {
    CompiledComputePipeline blazerod$compilePipelineCached(ComputePipeline pipeline);
}
