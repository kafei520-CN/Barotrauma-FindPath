package cn.kafei.bfindpath.path;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

/**
 * Overlap of one posed plot cube with one world cell. Shared faces do not count.
 */
public final class PosedCube {
    private static final double EDGE = 1.0e-4;
    private static final ThreadLocal<double[]> CLIP = ThreadLocal.withInitial(() -> new double[2]);

    private PosedCube() {
    }

    /**
     * Inclusive local block range covered by the world cell. False when the pose scale is zero
     * or the range is wider than a one-cell query should be.
     */
    public static boolean localRange(final Pose3dc pose, final int cellX, final int cellY, final int cellZ, final int[] out) {
        final Vector3dc scale = pose.scale();
        if (scale.x() == 0.0 || scale.y() == 0.0 || scale.z() == 0.0) {
            return false;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int corner = 0; corner < 8; corner++) {
            final double x = (corner & 1) == 0 ? cellX : cellX + 1.0;
            final double y = (corner & 2) == 0 ? cellY : cellY + 1.0;
            final double z = (corner & 4) == 0 ? cellZ : cellZ + 1.0;
            final double lx = inverse(pose, 0, x, y, z);
            final double ly = inverse(pose, 1, x, y, z);
            final double lz = inverse(pose, 2, x, y, z);
            minX = Math.min(minX, lx);
            minY = Math.min(minY, ly);
            minZ = Math.min(minZ, lz);
            maxX = Math.max(maxX, lx);
            maxY = Math.max(maxY, ly);
            maxZ = Math.max(maxZ, lz);
        }
        out[0] = floor(minX);
        out[1] = floor(minY);
        out[2] = floor(minZ);
        out[3] = floor(maxX);
        out[4] = floor(maxY);
        out[5] = floor(maxZ);
        return out[3] - out[0] <= 4 && out[4] - out[1] <= 4 && out[5] - out[2] <= 4;
    }

    private static final double COLUMN_INSET = 0.35;

    /**
     * True when a vertical column through the inner part of the cell runs through this cube for at least half a block.
     * A side graze does not count, so the air beside a wall stays open and vanilla fall limits apply.
     */
    public static boolean blocksColumn(
            final Pose3dc pose,
            final int localX,
            final int localY,
            final int localZ,
            final int cellX,
            final int cellY,
            final int cellZ
    ) {
        return column(pose, localX, localY, localZ, cellX + 0.5, cellZ + 0.5, cellY) >= 0.5
                || column(pose, localX, localY, localZ, cellX + COLUMN_INSET, cellZ + COLUMN_INSET, cellY) >= 0.5
                || column(pose, localX, localY, localZ, cellX + COLUMN_INSET, cellZ + 1.0 - COLUMN_INSET, cellY) >= 0.5
                || column(pose, localX, localY, localZ, cellX + 1.0 - COLUMN_INSET, cellZ + COLUMN_INSET, cellY) >= 0.5
                || column(pose, localX, localY, localZ, cellX + 1.0 - COLUMN_INSET, cellZ + 1.0 - COLUMN_INSET, cellY) >= 0.5;
    }

    private static double column(final Pose3dc pose, final int localX, final int localY, final int localZ, final double worldX, final double worldZ, final int cellY) {
        final double x0 = inverse(pose, 0, worldX, cellY, worldZ);
        final double y0 = inverse(pose, 1, worldX, cellY, worldZ);
        final double z0 = inverse(pose, 2, worldX, cellY, worldZ);
        final double x1 = inverse(pose, 0, worldX, cellY + 1.0, worldZ);
        final double y1 = inverse(pose, 1, worldX, cellY + 1.0, worldZ);
        final double z1 = inverse(pose, 2, worldX, cellY + 1.0, worldZ);
        return clipSpan(x0, y0, z0, x1 - x0, y1 - y0, z1 - z0, localX, localY, localZ);
    }

    private static double clipSpan(
            final double x, final double y, final double z,
            final double dx, final double dy, final double dz,
            final int boxX, final int boxY, final int boxZ
    ) {
        if (!slab(0.0, 1.0, x, dx, boxX, boxX + 1.0)) {
            return 0.0;
        }
        final double[] span = CLIP.get();
        if (!slab(span[0], span[1], y, dy, boxY, boxY + 1.0)) {
            return 0.0;
        }
        if (!slab(span[0], span[1], z, dz, boxZ, boxZ + 1.0)) {
            return 0.0;
        }
        return span[1] - span[0];
    }

    private static boolean slab(final double enter, final double exit, final double origin, final double delta, final double min, final double max) {
        final double[] span = CLIP.get();
        if (Math.abs(delta) < EDGE) {
            if (origin < min || origin > max) {
                return false;
            }
            span[0] = enter;
            span[1] = exit;
            return true;
        }
        double near = (min - origin) / delta;
        double far = (max - origin) / delta;
        if (near > far) {
            final double swap = near;
            near = far;
            far = swap;
        }
        span[0] = Math.max(enter, near);
        span[1] = Math.min(exit, far);
        return span[0] <= span[1];
    }

    private static double world(final Pose3dc pose, final int axis, final double x, final double y, final double z) {
        final Vector3dc pivot = pose.rotationPoint();
        final Vector3dc scale = pose.scale();
        final double rotated = rotate(
                pose.orientation(),
                axis,
                (x - pivot.x()) * scale.x(),
                (y - pivot.y()) * scale.y(),
                (z - pivot.z()) * scale.z(),
                1.0
        );
        final Vector3dc origin = pose.position();
        return rotated + (axis == 0 ? origin.x() : axis == 1 ? origin.y() : origin.z());
    }

    private static double inverse(final Pose3dc pose, final int axis, final double x, final double y, final double z) {
        final Vector3dc origin = pose.position();
        final Vector3dc scale = pose.scale();
        final double rotated = rotate(pose.orientation(), axis, x - origin.x(), y - origin.y(), z - origin.z(), -1.0);
        final double scaled = rotated / (axis == 0 ? scale.x() : axis == 1 ? scale.y() : scale.z());
        final Vector3dc pivot = pose.rotationPoint();
        return scaled + (axis == 0 ? pivot.x() : axis == 1 ? pivot.y() : pivot.z());
    }

    private static double rotate(final Quaterniondc q, final int axis, final double x, final double y, final double z, final double sign) {
        final double tx = sign * 2.0 * (q.y() * z - q.z() * y);
        final double ty = sign * 2.0 * (q.z() * x - q.x() * z);
        final double tz = sign * 2.0 * (q.x() * y - q.y() * x);
        final double rx = x + q.w() * tx + sign * (q.y() * tz - q.z() * ty);
        final double ry = y + q.w() * ty + sign * (q.z() * tx - q.x() * tz);
        final double rz = z + q.w() * tz + sign * (q.x() * ty - q.y() * tx);
        return axis == 0 ? rx : axis == 1 ? ry : rz;
    }

    private static int floor(final double value) {
        final int asInt = (int) value;
        return value < asInt ? asInt - 1 : asInt;
    }
}
