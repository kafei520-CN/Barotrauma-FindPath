package cn.kafei.bfindpath.path;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * World-cell occupancy of posed sub-levels.
 * Each query asks Sable's spatial index once, then checks those hits.
 */
public final class OccupyingBlocks {
    private OccupyingBlocks() {
    }

    /**
     * Plot storage cells become the posed world cell. World cells stay themselves.
     */
    public static BlockPos toWorldTarget(final Level level, final BlockPos pos) {
        final SubLevel containing = Sable.HELPER.getContaining(level, pos);
        if (containing == null) {
            return pos;
        }
        return BlockPos.containing(containing.logicalPose().transformPosition(pos.getCenter()));
    }

    /**
     * Solid plot block whose pose covers this world cell, or null.
     */
    @Nullable
    public static BlockState occupyingState(final Level level, final BlockPos worldPos) {
        final BoundingBox3d cell = new BoundingBox3d(
                worldPos.getX(), worldPos.getY(), worldPos.getZ(),
                worldPos.getX() + 1.0, worldPos.getY() + 1.0, worldPos.getZ() + 1.0);
        final Vec3 center = worldPos.getCenter();

        for (final SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, cell)) {
            final BlockPos localPos = BlockPos.containing(subLevel.logicalPose().transformPositionInverse(center));
            if (Sable.HELPER.getContaining(level, localPos) != subLevel) {
                continue;
            }
            final BlockState state = level.getBlockState(localPos);
            if (!state.isAir()) {
                return state;
            }
        }
        return null;
    }

    public static BlockState blockOrOccupying(final Level level, final BlockPos worldPos, final BlockState worldState) {
        if (!worldState.isAir()) {
            return worldState;
        }
        final BlockState occupying = occupyingState(level, worldPos);
        return occupying != null ? occupying : worldState;
    }
}
