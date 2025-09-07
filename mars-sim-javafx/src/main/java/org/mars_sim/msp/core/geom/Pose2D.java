package org.mars_sim.msp.core.geom;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable rigid transform (pose) in 2-D: translation (x,y) in world space and
 * heading {@code theta} in radians (counterclockwise, normalized to [-π, π]).
 * <p>
 * Provides fast helpers to transform between building-local and world frames.
 */
public final class Pose2D implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private final double x;       // world X (meters)
    private final double y;       // world Y (meters)
    private final double theta;   // radians, normalized to [-π, π]
    private final double cos;     // precomputed cos(theta)
    private final double sin;     // precomputed sin(theta)

    /**
     * Constructs a pose at world (x,y) with heading {@code thetaRad} (radians, CCW).
     * The angle is normalized to [-π, π] and cos/sin are precomputed for fast transforms.
     */
    public Pose2D(double x, double y, double thetaRad) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(thetaRad)) {
            throw new IllegalArgumentException("Pose2D components must be finite: "
                    + "x=" + x + ", y=" + y + ", theta=" + thetaRad);
        }
        double t = normalizeRadians(thetaRad);
        this.x = x;
        this.y = y;
        this.theta = t;
        this.cos  = Math.cos(t);
        this.sin  = Math.sin(t);
    }

    /** Factory using degrees (convenience). */
    public static Pose2D fromDegrees(double x, double y, double thetaDeg) {
        return new Pose2D(x, y, Math.toRadians(thetaDeg));
    }

    /** Returns a new pose translated by (dx, dy) in world space. */
    public Pose2D translated(double dx, double dy) {
        return new Pose2D(this.x + dx, this.y + dy, this.theta);
    }

    /** Returns a new pose rotated by {@code dThetaRad} (about its own origin). */
    public Pose2D rotated(double dThetaRad) {
        return new Pose2D(this.x, this.y, this.theta + dThetaRad);
    }

    /** World X in meters. */
    public double x() { return x; }

    /** World Y in meters. */
    public double y() { return y; }

    /** Heading in radians (CCW), normalized to [-π, π]. */
    public double theta() { return theta; }

    /** cos(theta) (precomputed). */
    public double cos() { return cos; }

    /** sin(theta) (precomputed). */
    public double sin() { return sin; }

    /**
     * Transforms a point from building-local coordinates into world/settlement coordinates.
     *
     * @param local point in the building's local frame
     * @return point in the world frame
     */
    public SettlementPos toSettlement(BuildingPos local) {
        Objects.requireNonNull(local, "local");
        double X = x +  cos * local.x() - sin * local.y();
        double Y = y +  sin * local.x() + cos * local.y();
        return new SettlementPos(X, Y);
    }

    /**
     * Transforms a point from world/settlement coordinates into building-local coordinates.
     *
     * @param world point in the world frame
     * @return point in the building's local frame
     */
    public BuildingPos toBuilding(SettlementPos world) {
        Objects.requireNonNull(world, "world");
        double dx = world.x() - x;
        double dy = world.y() - y;
        // inverse rotation by theta
        double lx =  cos * dx + sin * dy;
        double ly = -sin * dx + cos * dy;
        return new BuildingPos(lx, ly);
    }

    /** True if a local point lies inside an axis-aligned rectangle centered at (0,0). */
    public static boolean insideBuildingRect(BuildingPos local, double width, double length) {
        return local.insideAxisAlignedRect(width, length);
    }

    /** Normalizes an angle (radians) to [-π, π]. */
    public static double normalizeRadians(double radians) {
        // IEEEremainder yields (-π, π]; shift to [-π, π]
        double twoPi = Math.PI * 2.0;
        double t = Math.IEEEremainder(radians, twoPi);
        if (t <= -Math.PI) t += twoPi;
        if (t >   Math.PI) t -= twoPi;
        return t;
    }

    @Override
    public String toString() {
        return "Pose2D[x=" + x + ", y=" + y + ", thetaRad=" + theta + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pose2D other)) return false;
        // Exact equality on doubles is OK for immutable value objects that
        // are typically built from identical inputs; callers needing tolerance
        // should compare numerically with an epsilon.
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
            && Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y)
            && Double.doubleToLongBits(theta) == Double.doubleToLongBits(other.theta);
    }

    @Override
    public int hashCode() {
        long bits = 17;
        bits = 31 * bits + Double.doubleToLongBits(x);
        bits = 31 * bits + Double.doubleToLongBits(y);
        bits = 31 * bits + Double.doubleToLongBits(theta);
        return (int)(bits ^ (bits >>> 32));
    }
}
