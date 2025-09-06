package org.mars_sim.msp.core.geom;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable 2-D coordinate expressed in the settlement/world frame (meters).
 * Keep this type distinct from {@link BuildingPos} to avoid frame mix-ups.
 */
public record SettlementPos(double x, double y) implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    public SettlementPos {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("SettlementPos coordinates must be finite: x=" + x + ", y=" + y);
        }
    }

    /** Returns a new world position translated by (dX, dY) in world space. */
    public SettlementPos add(double dX, double dY) {
        return new SettlementPos(this.x + dX, this.y + dY);
    }

    /** Squared Euclidean distance to another world point (no sqrt). */
    public double distance2(SettlementPos other) {
        Objects.requireNonNull(other, "other");
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    /** Euclidean distance to another world point (meters). */
    public double distance(SettlementPos other) {
        return Math.sqrt(distance2(other));
    }
}
