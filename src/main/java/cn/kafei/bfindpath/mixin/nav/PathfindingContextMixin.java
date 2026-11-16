package cn.kafei.bfindpath.mixin.nav;

import cn.kafei.bfindpath.path.OccupyingBlocks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * World-grid pathfinding sees posed sub-level voxels, so mobs can walk onto and off them.
 */
@Mixin(PathfindingContext.class)
public class PathfindingContextMixin {
    @Shadow
    @Final
    @Mutable
    private BlockPos mobPosition;

    @Unique
    private Level bfindpath$level;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bfindpath$captureLevel(final CollisionGetter collisionGetter, final Mob mob, final CallbackInfo ci) {
        this.bfindpath$level = mob.level();
        this.mobPosition = BlockPos.containing(mob.position());
    }

    @ModifyReturnValue(method = "getBlockState", at = @At("RETURN"))
    private BlockState bfindpath$occupyingBlock(final BlockState original, final BlockPos pos) {
        if (this.bfindpath$level == null || !original.isAir()) {
            return original;
        }
        return OccupyingBlocks.blockOrOccupying(this.bfindpath$level, pos, original);
    }

    @Inject(method = "getPathTypeFromState", at = @At("HEAD"), cancellable = true)
    private void bfindpath$occupyingPathType(final int x, final int y, final int z, final CallbackInfoReturnable<PathType> cir) {
        if (this.bfindpath$level == null) {
            return;
        }
        final BlockState occupying = OccupyingBlocks.occupyingState(this.bfindpath$level, new BlockPos(x, y, z));
        if (occupying == null) {
            return;
        }
        final boolean empty = occupying.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
        cir.setReturnValue(empty ? PathType.OPEN : PathType.BLOCKED);
    }
}
