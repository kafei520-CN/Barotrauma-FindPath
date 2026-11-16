package cn.kafei.bfindpath.mixin.ai;

import cn.kafei.bfindpath.path.TrackingFrames;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Plot-cell look targets are projected back into world space.
 */
@Mixin(LookControl.class)
public class LookTargetMixin {
    @Shadow
    @Final
    protected Mob mob;

    @WrapMethod(method = "setLookAt(DDDFF)V")
    private void bfindpath$lookAtWorld(final double x, final double y, final double z, final float yawSpeed, final float pitchSpeed, final Operation<Void> original) {
        final SubLevel subLevel = TrackingFrames.tracking(this.mob);
        if (subLevel != null && Sable.HELPER.getContaining(this.mob.level(), BlockPos.containing(x, y, z)) == subLevel) {
            final Vec3 world = TrackingFrames.toWorld(subLevel, new Vec3(x, y, z));
            original.call(world.x, world.y, world.z, yawSpeed, pitchSpeed);
            return;
        }
        original.call(x, y, z, yawSpeed, pitchSpeed);
    }
}
