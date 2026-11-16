package cn.kafei.bfindpath.mixin.ai;

import cn.kafei.bfindpath.path.LocalQueryScope;
import cn.kafei.bfindpath.path.TrackingFrames;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Remaps grid queries into plot space while a local-query scope is active.
 * The entity's world position is unchanged.
 */
@Mixin(Entity.class)
public class LocalBlockQueryMixin {
    @Inject(method = "blockPosition()Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), cancellable = true)
    private void bfindpath$localBlockPosition(final CallbackInfoReturnable<BlockPos> cir) {
        final BlockPos local = this.bfindpath$localQueryPos();
        if (local != null) {
            cir.setReturnValue(local);
        }
    }

    @Inject(method = "chunkPosition()Lnet/minecraft/world/level/ChunkPos;", at = @At("HEAD"), cancellable = true)
    private void bfindpath$localChunkPosition(final CallbackInfoReturnable<ChunkPos> cir) {
        final BlockPos local = this.bfindpath$localQueryPos();
        if (local != null) {
            cir.setReturnValue(new ChunkPos(local));
        }
    }

    @Inject(method = "getBlockX", at = @At("HEAD"), cancellable = true)
    private void bfindpath$localBlockX(final CallbackInfoReturnable<Integer> cir) {
        final BlockPos local = this.bfindpath$localQueryPos();
        if (local != null) {
            cir.setReturnValue(local.getX());
        }
    }

    @Inject(method = "getBlockY", at = @At("HEAD"), cancellable = true)
    private void bfindpath$localBlockY(final CallbackInfoReturnable<Integer> cir) {
        final BlockPos local = this.bfindpath$localQueryPos();
        if (local != null) {
            cir.setReturnValue(local.getY());
        }
    }

    @Inject(method = "getBlockZ", at = @At("HEAD"), cancellable = true)
    private void bfindpath$localBlockZ(final CallbackInfoReturnable<Integer> cir) {
        final BlockPos local = this.bfindpath$localQueryPos();
        if (local != null) {
            cir.setReturnValue(local.getZ());
        }
    }

    @Inject(method = "getOnPos(F)Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), cancellable = true)
    private void bfindpath$localOnPos(final float yOffset, final CallbackInfoReturnable<BlockPos> cir) {
        final BlockPos local = this.bfindpath$localQueryPos();
        if (local != null) {
            cir.setReturnValue(local.below());
        }
    }

    @Unique
    private BlockPos bfindpath$localQueryPos() {
        if (!LocalQueryScope.active()) {
            return null;
        }
        final Entity self = (Entity) (Object) this;
        final SubLevel subLevel = TrackingFrames.tracking(self);
        if (subLevel == null) {
            return null;
        }
        return TrackingFrames.localBlockPos(self, subLevel);
    }
}
