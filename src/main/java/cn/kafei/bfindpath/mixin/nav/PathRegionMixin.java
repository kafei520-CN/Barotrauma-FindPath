package cn.kafei.bfindpath.mixin.nav;

import cn.kafei.bfindpath.path.LocalQueryScope;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;

/**
 * Path search must use the mob's world cell.
 * Goal ticks remap blockPosition into the plot, which puts the start node off the physics body.
 */
@Mixin(PathNavigation.class)
public class PathRegionMixin {
    @WrapMethod(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;")
    private Path bfindpath$worldMobCell(
            final Set<BlockPos> targets,
            final int regionOffset,
            final boolean offsetUpward,
            final int accuracy,
            final float followRange,
            final Operation<Path> original
    ) {
        LocalQueryScope.suppress();
        try {
            return original.call(targets, regionOffset, offsetUpward, accuracy, followRange);
        } finally {
            LocalQueryScope.restore();
        }
    }
}
