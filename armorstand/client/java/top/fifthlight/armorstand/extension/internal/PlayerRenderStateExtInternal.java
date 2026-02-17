package top.fifthlight.armorstand.extension.internal;

import org.jetbrains.annotations.Nullable;
import top.fifthlight.blazerod.render.api.animation.AnimationItemPendingValues;

import java.util.UUID;

public interface PlayerRenderStateExtInternal {
    void armorstand$setUuid(UUID uuid);

    UUID armorstand$getUuid();

    void armorstand$setAnimationPendingValues(AnimationItemPendingValues pendingValues);

    @Nullable
    AnimationItemPendingValues armorstand$getAnimationPendingValues();
}
