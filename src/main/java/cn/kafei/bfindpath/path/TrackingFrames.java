package cn.kafei.bfindpath.path;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Plot-local frames for a mob that is tracking a sub-level.
 * The entity position stays in world space.
 */
public final class TrackingFrames {
    private TrackingFrames() {
    }

    @Nullable
    public static SubLevel tracking(final Entity entity) {
        return Sable.HELPER.getTrackingSubLevel(entity);
    }

    public static BlockPos localBlockPos(final Entity entity, final SubLevel subLevel) {
        return BlockPos.containing(subLevel.logicalPose().transformPositionInverse(entity.position()));
    }

    public static Vec3 toWorld(final SubLevel subLevel, final Vec3 local) {
        return subLevel.logicalPose().transformPosition(local);
    }
}
