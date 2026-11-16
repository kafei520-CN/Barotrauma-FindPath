package cn.kafei.bfindpath.mixin.nav;

import cn.kafei.bfindpath.path.OccupyingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Posed targets skip the world-chunk air search. PathNavigation.createPath does the walk.
 */
@Mixin(GroundPathNavigation.class)
public abstract class GroundPathNavigationMixin extends PathNavigation {
    protected GroundPathNavigationMixin(final Mob mob, final Level level) {
        super(mob, level);
    }

    @Inject(method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("HEAD"), cancellable = true)
    private void bfindpath$worldTarget(final BlockPos blockPos, final int accuracy, final CallbackInfoReturnable<Path> cir) {
        final BlockPos world = OccupyingBlocks.toWorldTarget(this.level, blockPos);
        final boolean posed = !world.equals(blockPos)
                || OccupyingBlocks.occupyingState(this.level, world) != null
                || OccupyingBlocks.occupyingState(this.level, world.below()) != null;
        if (!posed) {
            return;
        }
        cir.setReturnValue(super.createPath(world, accuracy));
    }
}
