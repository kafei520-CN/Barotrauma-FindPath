package cn.kafei.bfindpath.mixin.ai;

import cn.kafei.bfindpath.path.LocalQueryScope;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Brain ticks read plot-local block positions for POI, work, and sleep.
 */
@Mixin(Brain.class)
public class BrainTickMixin {
    @WrapMethod(method = "tick")
    private void bfindpath$localBlockQueries(final ServerLevel level, final LivingEntity entity, final Operation<Void> original) {
        LocalQueryScope.begin();
        try {
            original.call(level, entity);
        } finally {
            LocalQueryScope.end();
        }
    }
}
