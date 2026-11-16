package cn.kafei.bfindpath.path;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * World-cell occupancy of posed sub-levels.
 * Each query asks the spatial index once, then checks the few local blocks that can meet that cell.
 */
public final class OccupyingBlocks {
    private static final ThreadLocal<int[]> LOCAL_RANGE = ThreadLocal.withInitial(() -> new int[6]);
    private static final ThreadLocal<MutableBlockPos> LOCAL_POS = ThreadLocal.withInitial(MutableBlockPos::new);

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
     * Plot block that fills at least half of this world cell, or null.
     */
    @Nullable
    public static BlockState occupyingState(final Level level, final BlockPos worldPos) {
        final BoundingBox3d cell = new BoundingBox3d(
                worldPos.getX(), worldPos.getY(), worldPos.getZ(),
                worldPos.getX() + 1.0, worldPos.getY() + 1.0, worldPos.getZ() + 1.0);
        for (final SubLevel subLevel : Sable.HELPER.getAllIntersecting(level, cell)) {
            final BlockState state = overlapping(level, subLevel, worldPos);
            if (state != null) {
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

    @Nullable
    private static BlockState overlapping(final Level level, final SubLevel subLevel, final BlockPos worldPos) {
        final Pose3dc pose = subLevel.logicalPose();
        final int[] range = LOCAL_RANGE.get();
        if (!PosedCube.localRange(pose, worldPos.getX(), worldPos.getY(), worldPos.getZ(), range)) {
            return centerHit(level, subLevel, worldPos.getCenter());
        }

        final MutableBlockPos localPos = LOCAL_POS.get();
        for (int x = range[0]; x <= range[3]; x++) {
            for (int y = range[1]; y <= range[4]; y++) {
                for (int z = range[2]; z <= range[5]; z++) {
                    localPos.set(x, y, z);
                    if (Sable.HELPER.getContaining(level, localPos) != subLevel) {
                        continue;
                    }
                    final BlockState state = level.getBlockState(localPos);
                    if (state.isAir()) {
                        continue;
                    }
                    if (PosedCube.blocksColumn(pose, x, y, z, worldPos.getX(), worldPos.getY(), worldPos.getZ())) {
                        return state;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static BlockState centerHit(final Level level, final SubLevel subLevel, final Vec3 center) {
        final BlockPos localPos = BlockPos.containing(subLevel.logicalPose().transformPositionInverse(center));
        if (Sable.HELPER.getContaining(level, localPos) != subLevel) {
            return null;
        }
        final BlockState state = level.getBlockState(localPos);
        return state.isAir() ? null : state;
    }
}
