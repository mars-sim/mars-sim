// mars-sim-core/src/main/java/org/mars_sim/msp/core/geom/FrameTransforms.java
package org.mars_sim.msp.core.geom;

public final class FrameTransforms {
  private FrameTransforms() {}

  /** Local building (x,y) -> world/settlement (X,Y). */
  public static SettlementPos toSettlement(BuildingPos pLocal, Pose2D pose) {
    double X = pose.x() + pose.cos()*pLocal.x() - pose.sin()*pLocal.y();
    double Y = pose.y() + pose.sin()*pLocal.x() + pose.cos()*pLocal.y();
    return new SettlementPos(X, Y);
  }

  /** World/settlement (X,Y) -> local building (x,y). */
  public static BuildingPos toBuilding(SettlementPos pWorld, Pose2D pose) {
    double dx = pWorld.x() - pose.x(), dy = pWorld.y() - pose.y();
    double x =  pose.cos()*dx + pose.sin()*dy;    // inverse rotation
    double y = -pose.sin()*dx + pose.cos()*dy;
    return new BuildingPos(x, y);
  }

  /** Axis-aligned rectangle containment in *building* space (fast path). */
  public static boolean insideBuilding(BuildingPos p, double width, double length) {
    double hx = width  * 0.5;   // width along X_local
    double hy = length * 0.5;   // length along Y_local
    return (p.x() >= -hx && p.x() <= hx && p.y() >= -hy && p.y() <= hy);
  }
}
