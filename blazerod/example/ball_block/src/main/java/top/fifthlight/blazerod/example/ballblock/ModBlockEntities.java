package top.fifthlight.blazerod.example.ballblock;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

// Shamelessly copied from https://docs.fabricmc.net/zh_cn/develop/blocks/block-entities
public class ModBlockEntities {
    public static final BlockEntityType<BallBlockEntity> BALL_BLOCK_ENTITY = register("ball", BallBlockEntity::new, ModBlocks.BALL);

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        var id = ResourceLocation.fromNamespaceAndPath(BallBlockMod.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }
}
