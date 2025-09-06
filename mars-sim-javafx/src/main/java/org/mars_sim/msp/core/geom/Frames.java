// mars-sim-core/src/main/java/org/mars_sim/msp/core/geom/Frames.java
package org.mars_sim.msp.core.geom;

public record SettlementPos(double x, double y) { }    // world/settlement frame
public record BuildingPos(double x, double y) { }      // building-local frame

/** Building pose in world frame: center (x,y) and heading (radians, +CCW). */
public final class Pose2D {
  private final double x, y, theta, cos, sin;
  public Pose2D(double x, double y, double thetaRad) {
    this.x = x; this.y = y;
    // normalize angle to [-π, π] to avoid drift
    this.theta = Math.IEEEremainder(thetaRad, 2*Math.PI);
    this.cos = Math.cos(this.theta);
    this.sin = Math.sin(this.theta);
  }
  public double x() { return x; }
  public double y() { return y; }
  public double theta() { return theta; }
  public double cos() { return cos; }
  public double sin() { return sin; }
}
