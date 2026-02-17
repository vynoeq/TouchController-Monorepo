package top.fifthlight.blazerod.render.version_1_21_8.extension.internal;

import com.mojang.blaze3d.opengl.GlDevice;
import top.fifthlight.blazerod.render.version_1_21_8.extension.CommandEncoderExt;
import top.fifthlight.blazerod.render.version_1_21_8.systems.ComputePass;

public interface CommandEncoderExtInternal extends CommandEncoderExt {
    GlDevice blazerod$getDevice();

    void blazerod$dispatchCompute(ComputePass pass, int x, int y, int z);
}
