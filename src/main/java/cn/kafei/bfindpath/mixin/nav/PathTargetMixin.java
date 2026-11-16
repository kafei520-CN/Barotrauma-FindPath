package cn.kafei.bfindpath.mixin.nav;

import cn.kafei.bfindpath.path.OccupyingBlocks;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Plot POI and bed targets become posed world cells, then vanilla pathfinding runs.
 */
@Mixin(PathNavigation.class)
public abstract class PathTargetMixin {
    @Shadow
    @Final
    protected Level level;

    @Shadow
    public abstract Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int reach, float followRange);

    @Inject(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("HEAD"), cancellable = true)
    private void bfindpath$worldTargets(final Set<BlockPos> targets, final int regionOffset, final boolean offsetUpward, final int reach, final float followRange, final CallbackInfoReturnable<Path> cir) {
        if (targets.isEmpty()) {
            return;
        }

        boolean converted = false;
        final Set<BlockPos> worldTargets = new ObjectOpenHashSet<>(targets.size());
        for (final BlockPos target : targets) {
            final BlockPos world = OccupyingBlocks.toWorldTarget(this.level, target);
            if (!world.equals(target)) {
                converted = true;
            }
            worldTargets.add(world);
        }

        if (converted) {
            cir.setReturnValue(this.createPath(worldTargets, regionOffset, offsetUpward, reach, followRange));
        }
    }
}
