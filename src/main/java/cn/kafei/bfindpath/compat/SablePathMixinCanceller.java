package cn.kafei.bfindpath.compat;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Sable NeoForge 2.0.6 (curse file 9043865) paths mobs in plot-local space.
 * Those mixins cannot be configured off. This addon supplies world-space occupancy instead.
 * Drop a name here if a later Sable build already paths in world space.
 */
public final class SablePathMixinCanceller implements MixinCanceller {
    private static final Logger LOGGER = LoggerFactory.getLogger("bfindpath");

    private static final Set<String> CANCEL = Set.of(
            "dev.ryanhcode.sable.mixin.entity.entity_ai.EatBlockGoalMixin",
            "dev.ryanhcode.sable.mixin.entity.entity_pathfinding.FlyNodeEvaluatorMixin",
            "dev.ryanhcode.sable.mixin.entity.entity_pathfinding.GroundPathNavigationMixin",
            "dev.ryanhcode.sable.mixin.entity.entity_pathfinding.PathMixin",
            "dev.ryanhcode.sable.mixin.entity.entity_pathfinding.PathNavigationMixin",
            "dev.ryanhcode.sable.mixin.entity.entity_pathfinding.PathfindingContextMixin",
            "dev.ryanhcode.sable.mixin.entity.entity_pathfinding.RandomPosMixin",
            "dev.ryanhcode.sable.mixin.entity.entity_pathfinding.WalkNodeEvaluatorMixin"
    );

    @Override
    public boolean shouldCancel(final List<String> targetClassNames, final String mixinClassName) {
        if (!CANCEL.contains(mixinClassName)) {
            return false;
        }
        LOGGER.info("Replacing Sable pathfinding mixin {}", mixinClassName);
        return true;
    }
}
