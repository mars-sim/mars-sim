package com.mars_sim.core.map.location;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.map.location.LocalBoundedObject;

/**
 * Immutable 2D rigid transform between a building-local frame and the settlement frame.
 * Local +X = building length axis; Local +Y = building width axis; origin = building center.
 */
public final class FrameTransform {

    private final double cos, sin;   // rotation from local->settlement (theta = facing degrees)
    private final double tx, ty;     // building center in settlement frame

    private FrameTransform(double facingDeg, LocalPosition center) {
        double theta = Math.toRadians(facingDeg);
        this.cos = Math.cos(theta);
        this.sin = Math.sin(theta);
        this.tx = center.getX();
        this.ty = center.getY();
    }

    /** Build a transform for a specific building/object. */
    public static FrameTransform forBuilding(LocalBoundedObject b) {
        return new FrameTransform(b.getFacing(), b.getPosition());
    }

    /** Convert building-local coordinates -> settlement coordinates. */
    public LocalPosition toSettlement(LocalPosition local) {
        double x = local.getX(), y = local.getY();
        double xs = tx + (cos * x - sin * y);
        double ys = ty + (sin * x + cos * y);
        return new LocalPosition(xs, ys);
    }

    /** Convert settlement coordinates -> building-local coordinates. */
    public LocalPosition toBuilding(LocalPosition settlement) {
        // inverse: translate to building center then rotate by -theta
        double dx = settlement.getX() - tx;
        double dy = settlement.getY() - ty;
        double xb =  cos * dx + sin * dy;
        double yb = -sin * dx + cos * dy;
        return new LocalPosition(xb, yb);
    }

    /** Axis-aligned containment test in the local frame (length on X, width on Y). */
    public boolean contains(LocalBoundedObject b, LocalPosition settlementPoint) {
        LocalPosition pLocal = toBuilding(settlementPoint);
        double hx = b.getLength() / 2.0; // local X half-extent
        double hy = b.getWidth()  / 2.0; // local Y half-extent
        return Math.abs(pLocal.getX()) <= hx && Math.abs(pLocal.getY()) <= hy;
    }
}
