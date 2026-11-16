package cn.kafei.bfindpath.mixin.ai;

import cn.kafei.bfindpath.path.LocalQueryScope;
import cn.kafei.bfindpath.path.TrackingFrames;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Grass checks use the plot cell. A local-query scope already remapped the position.
 */
@Mixin(EatBlockGoal.class)
public class EatBlockGoalMixin {
    @WrapOperation(method = {"tick", "canUse"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;blockPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos bfindpath$blockPosition(final Mob instance, final Operation<BlockPos> original) {
        if (LocalQueryScope.active()) {
            return original.call(instance);
        }

        final BlockPos pos = original.call(instance);
        final SubLevel subLevel = TrackingFrames.tracking(instance);
        if (subLevel != null) {
            return TrackingFrames.localBlockPos(instance, subLevel);
        }
        return pos;
    }
}
