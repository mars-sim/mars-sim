package org.mars_sim.msp.core.geom;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable 2-D coordinate expressed in a building's local frame (meters).
 * X_local grows to the building's "right", Y_local grows toward its "front".
 * <p>
 * Keep this type distinct from {@link SettlementPos} to avoid frame mix-ups.
 */
public record BuildingPos(double x, double y) implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    public BuildingPos {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("BuildingPos coordinates must be finite: x=" + x + ", y=" + y);
        }
    }

    /** Returns a new position translated by (dx, dy) in building-local space. */
    public BuildingPos add(double dx, double dy) {
        return new BuildingPos(this.x + dx, this.y + dy);
    }

    /** Squared Euclidean distance to another local point (no sqrt). */
    public double distance2(BuildingPos other) {
        Objects.requireNonNull(other, "other");
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    /** Euclidean distance to another local point (meters). */
    public double distance(BuildingPos other) {
        return Math.sqrt(distance2(other));
    }

    /**
     * True if this point lies within an axis-aligned rectangle centered at (0,0)
     * in building-local coordinates having the given {@code width} (X axis) and
     * {@code length} (Y axis).
     */
    public boolean insideAxisAlignedRect(double width, double length) {
        double hx = width * 0.5;
        double hy = length * 0.5;
        return (x >= -hx && x <= hx && y >= -hy && y <= hy);
    }
}
