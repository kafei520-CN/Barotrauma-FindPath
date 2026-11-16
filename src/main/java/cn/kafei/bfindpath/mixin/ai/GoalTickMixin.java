package cn.kafei.bfindpath.mixin.ai;

import cn.kafei.bfindpath.path.LocalQueryScope;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Goal ticks also read plot-local block positions.
 */
@Mixin(Mob.class)
public class GoalTickMixin {
    @WrapOperation(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;tick()V"))
    private void bfindpath$localGoalQueries(final GoalSelector instance, final Operation<Void> original) {
        LocalQueryScope.begin();
        try {
            original.call(instance);
        } finally {
            LocalQueryScope.end();
        }
    }
}
