package top.fifthlight.blazerod.render.version_1_21_8.extension.internal;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.fifthlight.blazerod.render.version_1_21_8.extension.RenderPassExt;

import java.util.Map;

public interface RenderPassExtInternal extends RenderPassExt {
    @Nullable
    VertexFormat blazerod$getVertexFormat();

    @Nullable
    VertexFormat.Mode blazerod$getVertexFormatMode();

    @NotNull
    Map<String, GpuBufferSlice> blazerod$getStorageBuffers();
}
