package top.fifthlight.blazerod.example.ballblock;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

// Shamelessly copied from https://docs.fabricmc.net/zh_cn/develop/blocks/block-entity-renderer
public class BallBlockEntityRenderer implements BlockEntityRenderer<BallBlockEntity> {
    public BallBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BallBlockEntity entity, float tickProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Vec3 cameraPos) {
        var instance = BallBlockMod.getBallInstance();
        if (instance == null) {
            return;
        }
        var client = Minecraft.getInstance();
        var frameBuffer = client.getMainRenderTarget();
        var colorFrameBuffer = RenderSystem.outputColorTextureOverride != null
                ? RenderSystem.outputColorTextureOverride
                : frameBuffer.getColorTextureView();
        var depthFrameBuffer = frameBuffer.useDepth
                ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : frameBuffer.getDepthTextureView())
                : null;
        var renderer = BallBlockMod.getRenderer();
        renderer.render(Objects.requireNonNull(colorFrameBuffer), depthFrameBuffer, instance.createRenderTask(matrices.last().pose(), light, 0), instance.getScene());
    }
}
