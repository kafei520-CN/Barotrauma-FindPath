package cn.kafei.bfindpath.mixin.nav;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Replaces Sable 2.0.6's plot-local wander offset.
 */
@Mixin(RandomPos.class)
public class WanderPosMixin {
    /**
     * @author bfindpath
     * @reason Wander in world space so posed sub-levels are valid walk targets.
     */
    @Overwrite
    public static BlockPos generateRandomPosTowardDirection(final PathfinderMob mob, final int horizontalRange, final RandomSource random, final BlockPos direction) {
        int offsetX = direction.getX();
        int offsetZ = direction.getZ();

        if (mob.hasRestriction() && horizontalRange > 1) {
            final BlockPos center = mob.getRestrictCenter();
            final int spread = random.nextInt(horizontalRange / 2);
            if (mob.getX() > (double) center.getX()) {
                offsetX -= spread;
            } else {
                offsetX += spread;
            }

            final int spreadZ = random.nextInt(horizontalRange / 2);
            if (mob.getZ() > (double) center.getZ()) {
                offsetZ -= spreadZ;
            } else {
                offsetZ += spreadZ;
            }
        }

        return BlockPos.containing((double) offsetX + mob.getX(), (double) direction.getY() + mob.getY(), (double) offsetZ + mob.getZ());
    }
}
