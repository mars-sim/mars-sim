/*
 * This file is part of the Mars Simulation Project (mars-sim).
 * License: GPL-3.0 (see the project root LICENSE file).
 *
 * Purpose:
 *   Lightweight two-body (Kepler) propagator for areocentric orbits.
 *   Produces Mars-centered inertial (MCI) state vectors and converts
 *   them to Mars-centered, Mars-fixed (MCMF) coordinates using a simple
 *   uniform-rotation model. Also provides a subsatellite point helper.
 *
 * Design notes:
 *   - No external deps; fast and deterministic.
 *   - Uses classical elements (a, e, i, Ω, ω, M0 @ epoch) in KeplerianElements.
 *   - MCI ← PQW transformation is pre-factored via unit vectors p̂ and q̂.
 *   - MCMF rotation uses θ(t) = θ0 + ωMars * (t - tθ0). By default we take
 *     θ0 = 0 at tθ0 = elements.epoch(), which keeps things simple/portable.
 *   - Geodetic vs. geocentric: the subsatellite point uses a spherical Mars
 *     (mean radius). This is appropriate for comms/coverage gameplay; you can
 *     later swap in an ellipsoidal model without changing callers.
 */

package org.mars_sim.msp.core.orbit;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public final class TwoBodyPropagator {

    // ---------------------------------------------------------------------
    // Nested data classes
    // ---------------------------------------------------------------------

    /** Immutable Cartesian state (position & velocity). */
    public static final class StateVector {
        /** Position [m] in chosen frame. */
        public final double[] r;
        /** Velocity [m/s] in chosen frame. */
        public final double[] v;

        public StateVector(double[] r, double[] v) {
            if (r == null || v == null || r.length != 3 || v.length != 3)
                throw new IllegalArgumentException("StateVector requires 3D r and v arrays.");
            this.r = new double[] { r[0], r[1], r[2] };
            this.v = new double[] { v[0], v[1], v[2] };
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT,
                    "State[r=(%.3f, %.3f, %.3f) m, v=(%.6f, %.6f, %.6f) m/s]",
                    r[0], r[1], r[2], v[0], v[1], v[2]);
        }
    }

    // ---------------------------------------------------------------------
    // Fields (immutable core)
    // ---------------------------------------------------------------------

    private final KeplerianElements kep;
    /** μ for Mars [m^3/s^2] (can be overridden in advanced use). */
    private final double mu;

    /** Precomputed orbital plane unit vectors (inertial frame). */
    private final double[] pHat; // unit vector toward periapsis
    private final double[] qHat; // 90° ahead in direction of motion

    /** Fixed frame rotation reference: θ(t) = θ0 + ω (t - tθ0). */
    private final Instant fixedFrameEpoch;
    private final double greenwichAtEpochRad;

    // ---------------------------------------------------------------------
    // Construction
    // ---------------------------------------------------------------------

    /**
     * Construct a propagator using Mars μ and with the fixed-frame rotation
     * referenced such that θ0 = 0 at the elements' epoch.
     */
    public TwoBodyPropagator(KeplerianElements elements) {
        this(elements, AreoBodyConstants.MARS_GM_M3_S2, elements.getEpoch(), 0.0);
    }

    /** Construct a propagator with a custom μ (still Mars-centered). */
    public TwoBodyPropagator(KeplerianElements elements, double mu) {
        this(elements, mu, elements.getEpoch(), 0.0);
    }

    /**
     * Full-control constructor.
     * @param elements                orbital elements (immutable)
     * @param mu                      gravitational parameter [m^3/s^2]
     * @param fixedFrameEpoch         epoch for θ0 (tθ0)
     * @param greenwichAtEpochRad     θ0 (Greenwich angle at tθ0), rad
     */
    public TwoBodyPropagator(KeplerianElements elements,
                             double mu,
                             Instant fixedFrameEpoch,
                             double greenwichAtEpochRad) {
        this.kep = Objects.requireNonNull(elements, "elements");
        if (!(mu > 0.0) || !Double.isFinite(mu)) {
            throw new IllegalArgumentException("μ must be finite and > 0.");
        }
        this.mu = mu;
        this.fixedFrameEpoch = Objects.requireNonNull(fixedFrameEpoch, "fixedFrameEpoch");
        this.greenwichAtEpochRad = greenwichAtEpochRad;

        // Precompute inertial unit vectors p̂, q̂ (perifocal basis rotated to MCI).
        double i = kep.getInclinationRad();
        double O = kep.getRaanRad();
        double w = kep.getArgOfPeriapsisRad();

        double cO = Math.cos(O), sO = Math.sin(O);
        double ci = Math.cos(i), si = Math.sin(i);
        double cw = Math.cos(w), sw = Math.sin(w);

        // p̂ = R3(O) R1(i) R3(w) [1, 0, 0]^T
        pHat = new double[] {
                cO * cw - sO * sw * ci,
                sO * cw + cO * sw * ci,
                sw * si
        };
        // q̂ = R3(O) R1(i) R3(w) [0, 1, 0]^T
        qHat = new double[] {
                -cO * sw - sO * cw * ci,
                -sO * sw + cO * cw * ci,
                cw * si
        };
    }

    // ---------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------

    public KeplerianElements elements() { return kep; }
    public double standardGravitationalParameter() { return mu; }
    public Instant fixedFrameEpoch() { return fixedFrameEpoch; }
    public double greenwichAtEpochRad() { return greenwichAtEpochRad; }

    // ---------------------------------------------------------------------
    // Propagation (MCI / inertial)
    // ---------------------------------------------------------------------

    /**
     * Propagate to time {@code t} and return the MCI (inertial) state.
     * Two-body, osculating, no perturbations.
     */
    public StateVector propagateInertial(Instant t) {
        // Time-dependent anomalies
        double e = kep.getEccentricity();
        double a = kep.getSemiMajorAxisM();
        double M = kep.meanAnomalyAt(t);
        double E = KeplerianElements.eccentricAnomalyFromMean(M, e);

        // Numerically stable PQW parameterization via E
        double cosE = Math.cos(E);
        double sinE = Math.sin(E);
        double oneMinusEcosE = 1.0 - e * cosE;
        double r = a * oneMinusEcosE;
        double beta = Math.sqrt(Math.max(0.0, 1.0 - e * e)); // √(1-e^2)

        // Position in PQW
        double x_pqw = a * (cosE - e);
        double y_pqw = a * beta * sinE;

        // Velocity in PQW
        double fact = Math.sqrt(mu * a) / r; // = n a^2 / r
        double vx_pqw = -fact * sinE;
        double vy_pqw =  fact * beta * cosE;

        // Rotate PQW -> MCI using precomputed p̂, q̂
        double[] rI = linComb2(x_pqw, pHat, y_pqw, qHat);
        double[] vI = linComb2(vx_pqw, pHat, vy_pqw, qHat);

        return new StateVector(rI, vI);
    }

    // ---------------------------------------------------------------------
    // Frame conversion (MCI -> MCMF)
    // ---------------------------------------------------------------------

    /**
     * Convert an inertial state at time {@code t} to Mars-fixed (MCMF).
     * Uses uniform rotation with rate AreoBodyConstants.MARS_ROTATION_RATE_RAD_S.
     */
    public StateVector inertialToFixed(StateVector inertial, Instant t) {
        double theta = greenwichAngleAt(t);
        double[] rF = rotateZ(theta, inertial.r);

        // v_fixed = Rz(θ) * (v_inertial - ω × r_inertial)
        double[] omega = new double[] { 0.0, 0.0, AreoBodyConstants.MARS_ROTATION_RATE_RAD_S };
        double[] vEff = sub3(inertial.v, cross3(omega, inertial.r));
        double[] vF = rotateZ(theta, vEff);

        return new StateVector(rF, vF);
    }

    /** Convenience: propagate to {@code t} and return the Mars-fixed state. */
    public StateVector propagateFixed(Instant t) {
        return inertialToFixed(propagateInertial(t), t);
    }

    // ---------------------------------------------------------------------
    // Subsatellite point (spherical Mars)
    // ---------------------------------------------------------------------

    /**
     * Returns the subsatellite point at time {@code t} on a spherical Mars with
     * mean radius. Longitude is East-positive in (-π, π].
     */
    public GroundPoint subsatellitePoint(Instant t) {
        StateVector sF = propagateFixed(t);
        return subsatellitePointFromFixedPosition(sF.r);
    }

    /** Utility: subsatellite point from a Mars-fixed position vector. */
    public static GroundPoint subsatellitePointFromFixedPosition(double[] rFixed) {
        double x = rFixed[0], y = rFixed[1], z = rFixed[2];
        double rho = Math.hypot(x, y);
        double rmag = Math.hypot(rho, z);

        double lat = Math.atan2(z, rho);      // geocentric latitude
        double lon = Math.atan2(y, x);        // East-positive
        double alt = rmag - AreoBodyConstants.MARS_MEAN_RADIUS_M;

        return new GroundPoint(lat, lon, alt);
    }

    // ---------------------------------------------------------------------
    // Rotation angle helper
    // ---------------------------------------------------------------------

    /**
     * Greenwich angle θ(t) used for inertial→fixed rotation:
     * θ(t) = θ0 + ωMars * (t - tθ0).
     */
    public double greenwichAngleAt(Instant t) {
        double dt = secondsBetween(fixedFrameEpoch, t);
        double theta = greenwichAtEpochRad + AreoBodyConstants.MARS_ROTATION_RATE_RAD_S * dt;
        // normalize to [0, 2π)
        double x = theta % AreoBodyConstants.TWO_PI;
        if (x < 0) x += AreoBodyConstants.TWO_PI;
        return x;
    }

    // ---------------------------------------------------------------------
    // Small vector/matrix utilities (no external deps)
    // ---------------------------------------------------------------------

    /** linear combination: a*u + b*v */
    private static double[] linComb2(double a, double[] u, double b, double[] v) {
        return new double[] {
                a * u[0] + b * v[0],
                a * u[1] + b * v[1],
                a * u[2] + b * v[2]
        };
    }

    /** r = Rz(angle) * v */
    private static double[] rotateZ(double angle, double[] v) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new double[] { c * v[0] - s * v[1], s * v[0] + c * v[1], v[2] };
        // Note: this matches the standard ECI->ECEF-like convention (eastward positive).
    }

    private static double[] cross3(double[] a, double[] b) {
        return new double[] {
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private static double[] sub3(double[] a, double[] b) {
        return new double[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
    }

    private static double secondsBetween(Instant t0, Instant t1) {
        long ds = t1.getEpochSecond() - t0.getEpochSecond();
        int dns = t1.getNano() - t0.getNano();
        return (double) ds + dns * 1e-9;
    }
}
