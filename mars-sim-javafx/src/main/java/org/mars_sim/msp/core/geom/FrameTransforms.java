package org.mars_sim.msp.core.geom;

import java.util.Objects;

/**
 * Static helpers to transform between coordinate frames:
 * - Building-local (x,y) {@link BuildingPos}
 * - Settlement/world (X,Y) {@link SettlementPos}
 *
 * These are thin wrappers around {@link Pose2D}'s instance methods, provided to
 * make call sites explicit and to aid discoverability during refactors.
 *
 * All dimensions are in meters; headings are counterclockwise (mathematical convention).
 */
public final class FrameTransforms {

    private FrameTransforms() {
        // utility class
    }

    /**
     * Convert a point from building-local coordinates to settlement/world coordinates.
     *
     * @param local point in the building's local frame (meters)
     * @param pose  building pose in world frame
     * @return world/settlement coordinates (meters)
     */
    public static SettlementPos toSettlement(BuildingPos local, Pose2D pose) {
        Objects.requireNonNull(local, "local");
        Objects.requireNonNull(pose, "pose");
        return pose.toSettlement(local);
        // X = x0 + cos*lx - sin*ly
        // Y = y0 + sin*lx + cos*ly
    }

    /**
     * Convert a point from settlement/world coordinates to building-local coordinates.
     *
     * @param world point in the world frame (meters)
     * @param pose  building pose in world frame
     * @return building-local coordinates (meters)
     */
    public static BuildingPos toBuilding(SettlementPos world, Pose2D pose) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(pose, "pose");
        return pose.toBuilding(world);
        // lx =  cos*dx + sin*dy
        // ly = -sin*dx + cos*dy
    }

    /**
     * Fast containment check for a building-local point inside an axis-aligned
     * rectangle centered at the building origin (0,0) in local coordinates.
     * <p>
     * The rectangle uses:
     * - {@code width}  along the building's local X axis
     * - {@code length} along the building's local Y axis
     *
     * @param local  point in building-local coordinates
     * @param width  size along local X (meters)
     * @param length size along local Y (meters)
     * @return true if the point lies inside or on the rectangle boundary
     */
    public static boolean insideBuilding(BuildingPos local, double width, double length) {
        Objects.requireNonNull(local, "local");
        if (!(Double.isFinite(width) && Double.isFinite(length)) || width < 0 || length < 0) {
            throw new IllegalArgumentException("width and length must be finite and non-negative: "
                    + "width=" + width + ", length=" + length);
        }
        return local.insideAxisAlignedRect(width, length);
    }

    // -----------------------------
    // Convenience helpers (optional)
    // -----------------------------

    /**
     * Transforms a local delta vector (dx, dy) into a world-space delta using the pose's rotation.
     * The building translation is not applied (pure rotation of the vector).
     */
    public static SettlementPos deltaToSettlement(double dxLocal, double dyLocal, Pose2D pose) {
        Objects.requireNonNull(pose, "pose");
        double dX =  pose.cos() * dxLocal - pose.sin() * dyLocal;
        double dY =  pose.sin() * dxLocal + pose.cos() * dyLocal;
        return new SettlementPos(dX, dY);
    }

    /**
     * Transforms a world delta vector (dX, dY) into a local delta using the inverse rotation.
     * The building translation is not applied (pure inverse rotation of the vector).
     */
    public static BuildingPos deltaToBuilding(double dXWorld, double dYWorld, Pose2D pose) {
        Objects.requireNonNull(pose, "pose");
        double dx =  pose.cos() * dXWorld + pose.sin() * dYWorld;
        double dy = -pose.sin() * dXWorld + pose.cos() * dYWorld;
        return new BuildingPos(dx, dy);
    }

    /**
     * Returns the four settlement/world-space corners of an axis-aligned rectangle
     * defined in building-local coordinates with the given width/length, centered at (0,0),
     * after applying the building pose. Order is CCW starting at (-w/2,-l/2).
     */
    public static SettlementPos[] buildingRectCornersToSettlement(double width, double length, Pose2D pose) {
        Objects.requireNonNull(pose, "pose");
        if (!(Double.isFinite(width) && Double.isFinite(length)) || width < 0 || length < 0) {
            throw new IllegalArgumentException("width and length must be finite and non-negative: "
                    + "width=" + width + ", length=" + length);
        }
        double hx = width * 0.5;
        double hy = length * 0.5;

        BuildingPos[] localCorners = new BuildingPos[] {
            new BuildingPos(-hx, -hy),
            new BuildingPos(+hx, -hy),
            new BuildingPos(+hx, +hy),
            new BuildingPos(-hx, +hy)
        };

        SettlementPos[] worldCorners = new SettlementPos[4];
        for (int i = 0; i < 4; i++) {
            worldCorners[i] = pose.toSettlement(localCorners[i]);
        }
        return worldCorners;
    }
}
