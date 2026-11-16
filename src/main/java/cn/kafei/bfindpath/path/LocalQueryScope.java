package cn.kafei.bfindpath.path;

/**
 * Marks the server thread while brain or goal ticks want plot-local block queries.
 */
public final class LocalQueryScope {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> SUPPRESS = ThreadLocal.withInitial(() -> 0);

    private LocalQueryScope() {
    }

    public static void begin() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void end() {
        final int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(depth);
        }
    }

    public static void suppress() {
        SUPPRESS.set(SUPPRESS.get() + 1);
    }

    public static void restore() {
        final int depth = SUPPRESS.get() - 1;
        if (depth <= 0) {
            SUPPRESS.remove();
        } else {
            SUPPRESS.set(depth);
        }
    }

    public static boolean active() {
        return DEPTH.get() > 0 && SUPPRESS.get() == 0;
    }
}
