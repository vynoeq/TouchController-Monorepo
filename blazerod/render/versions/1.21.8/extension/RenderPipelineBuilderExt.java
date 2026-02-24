package top.fifthlight.blazerod.render.version_1_21_8.extension;

import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.NotNull;

public interface RenderPipelineBuilderExt {
    void blazerod$withVertexFormat(@NotNull VertexFormat format);

    void blazerod$withStorageBuffer(@NotNull String name);
}
