package cn.kafei.bfindpath.mixin.nav;

import cn.kafei.bfindpath.path.OccupyingBlocks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Floor sampling includes posed sub-level cells so waypoints do not float.
 */
@Mixin(PathNavigation.class)
public abstract class GroundSampleMixin {
    @Shadow
    @Final
    protected Level level;

    @ModifyReturnValue(method = "getGroundY", at = @At("RETURN"))
    private double bfindpath$groundOnSubLevel(final double original, final Vec3 vec) {
        final BlockPos pos = BlockPos.containing(vec);
        if (!this.level.getBlockState(pos.below()).isAir()) {
            return original;
        }
        final BlockState occupying = OccupyingBlocks.occupyingState(this.level, pos.below());
        if (occupying != null && !occupying.getCollisionShape(this.level, pos.below()).isEmpty()) {
            return pos.getY();
        }
        return original;
    }
}
