package com.mars_sim.core.moon;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable descriptor for a natural satellite used by the simulation.
 *
 * <p>Design goals:
 * <ul>
 *   <li>Pure data + pure math. No dependency on global clocks or sim singletons.</li>
 *   <li>Thread-safe and immutable; safe to share across worker threads.</li>
 *   <li>All units are explicit (SI): meters, seconds, radians, kilograms.</li>
 *   <li>Only additive APIs; easy to preserve any existing public signature.</li>
 * </ul>
 *
 * <p>Orbital model notes:
 * <ul>
 *   <li>Simple Keplerian ellipse with classic elements; good enough for visualization,
 *       rough illumination estimates, scheduling, etc.</li>
 *   <li>Callers supply an epoch and a time to evaluate; the class has zero global state.</li>
 * </ul>
 */
public final class Moon implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    // ---- Identity & bulk properties ----
    private final String name;          // e.g., "Phobos", "Deimos"
    private final double bodyRadius;    // meters
    private final double bodyMass;      // kilograms

    // ---- Keplerian elements (parent-body equatorial frame, SI) ----
    private final double semiMajorAxis;           // meters (a)
    private final double eccentricity;            // [0, 1)
    private final double inclinationRad;          // radians (i)
    private final double longAscNodeRad;          // radians (Ω)
    private final double argPeriapsisRad;         // radians (ω)
    private final double meanLongitudeAtEpochRad; // radians (L0) = M0 + ϖ
    private final double siderealPeriod;          // seconds (T)

    // ---- Derived, cached ----
    private final double meanMotion; // radians per second, n = 2π/T

    private Moon(Builder b) {
        this.name                     = Objects.requireNonNull(b.name, "name");
        this.bodyRadius               = positive(b.bodyRadius, "bodyRadius");
        this.bodyMass                 = positive(b.bodyMass, "bodyMass");
        this.semiMajorAxis            = positive(b.semiMajorAxis, "semiMajorAxis");
        this.eccentricity             = clamp01(b.eccentricity);
        this.inclinationRad           = requireFinite(b.inclinationRad, "inclinationRad");
        this.longAscNodeRad           = requireFinite(b.longAscNodeRad, "longAscNodeRad");
        this.argPeriapsisRad          = requireFinite(b.argPeriapsisRad, "argPeriapsisRad");
        this.meanLongitudeAtEpochRad  = requireFinite(b.meanLongitudeAtEpochRad, "meanLongitudeAtEpochRad");
        this.siderealPeriod           = positive(b.siderealPeriod, "siderealPeriod");
        this.meanMotion               = 2.0 * Math.PI / this.siderealPeriod;

        // Final sanity
        if (Double.isNaN(meanMotion) || meanMotion <= 0) {
            throw new IllegalStateException("meanMotion invalid");
        }
    }

    // ------------------------- Public API (getters) -------------------------

    /** Unique moon name (identity). */
    public String getName() { return name; }

    /** Physical radius in meters. */
    public double getBodyRadius() { return bodyRadius; }

    /** Mass in kilograms. */
    public double getBodyMass() { return bodyMass; }

    /** Semi-major axis in meters. */
    public double getSemiMajorAxis() { return semiMajorAxis; }

    /** Eccentricity [0,1). */
    public double getEccentricity() { return eccentricity; }

    /** Inclination (radians). */
    public double getInclinationRad() { return inclinationRad; }

    /** Longitude of ascending node (radians). */
    public double getLongitudeAscendingNodeRad() { return longAscNodeRad; }

    /** Argument of periapsis (radians). */
    public double getArgumentOfPeriapsisRad() { return argPeriapsisRad; }

    /** Mean longitude at epoch (radians). */
    public double getMeanLongitudeAtEpochRad() { return meanLongitudeAtEpochRad; }

    /** Sidereal period in seconds. */
    public double getSiderealPeriodSeconds() { return siderealPeriod; }

    /** Mean motion (radians/second). */
    public double getMeanMotionRadPerSec() { return meanMotion; }

    // ------------------------- Orbital helpers (pure) -------------------------

    /**
     * Mean anomaly (radians) at a given elapsed time since epoch (seconds).
     * M = L - ϖ + n * Δt
     */
    public double meanAnomalyAtSecondsSinceEpoch(double secondsSinceEpoch) {
        final double L = meanLongitudeAtEpochRad + meanMotion * secondsSinceEpoch;
        final double varpi = longAscNodeRad + argPeriapsisRad; // longitude of periapsis
        return normalizeAngle(L - varpi);
    }

    /** Solve Kepler's equation (elliptical) for eccentric anomaly E (radians). */
    public double eccentricAnomaly(double meanAnomaly) {
        final double e = eccentricity;
        if (e == 0.0) return normalizeAngle(meanAnomaly); // circular case
        double E = meanAnomaly; // initial guess
        for (int i = 0; i < 8; i++) {
            final double f  = E - e * Math.sin(E) - meanAnomaly;
            final double fp = 1.0 - e * Math.cos(E);
            E -= f / fp;
        }
        return normalizeAngle(E);
    }

    /** True anomaly ν (radians) from eccentric anomaly E (radians). */
    public double trueAnomaly(double E) {
        final double e = eccentricity;
        final double cosE = Math.cos(E);
        final double sinE = Math.sin(E);
        final double denom = 1.0 - e * cosE;
        final double cosV = (cosE - e) / denom;
        final double sinV = (Math.sqrt(1.0 - e * e) * sinE) / denom;
        return Math.atan2(sinV, cosV);
    }

    /** Radius r (meters) in orbital plane at eccentric anomaly E. */
    public double radiusAtE(double E) {
        return semiMajorAxis * (1.0 - eccentricity * Math.cos(E));
    }

    /**
     * Position vector (meters) in parent-body equatorial frame at a given time.
     * Rotation order: Rz(Ω) · Rx(i) · Rz(ω).
     *
     * @param epoch reference epoch (t0)
     * @param when  absolute time of interest
     * @return double[3] = {x, y, z} in meters.
     */
    public double[] positionAt(Instant epoch, Instant when) {
        Objects.requireNonNull(epoch, "epoch");
        Objects.requireNonNull(when, "when");

        final double dt = (double) Duration.between(epoch, when).toSeconds();
        final double M  = meanAnomalyAtSecondsSinceEpoch(dt);
        final double E  = eccentricAnomaly(M);
        final double v  = trueAnomaly(E);
        final double r  = radiusAtE(E);

        // 2D in orbital plane
        final double xOrb = r * Math.cos(v);
        final double yOrb = r * Math.sin(v);

        // rotate by ω (periapsis), i (inclination), Ω (node)
        final double cosO = Math.cos(longAscNodeRad), sinO = Math.sin(longAscNodeRad);
        final double cosI = Math.cos(inclinationRad),  sinI = Math.sin(inclinationRad);
        final double cosw = Math.cos(argPeriapsisRad), sinw = Math.sin(argPeriapsisRad);

        final double x1 =  cosw * xOrb - sinw * yOrb;
        final double y1 =  sinw * xOrb + cosw * yOrb;

        final double x2 =  x1;
        final double y2 =  cosI * y1;
        final double z2 =  sinI * y1;

        final double x  =  cosO * x2 - sinO * y2;
        final double y  =  sinO * x2 + cosO * y2;
        final double z  =  z2;

        return new double[] { x, y, z };
    }

    // ------------------------- Value semantics -------------------------

    /** Identity is by name; assumes names are unique across moons in the simulation. */
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Moon m)) return false;
        return name.equals(m.name);
    }

    @Override public int hashCode() { return name.hashCode(); }

    @Override public String toString() { return "Moon[" + name + "]"; }

    // ------------------------- Builder -------------------------

    public static Builder builder(String name) { return new Builder(name); }

    /**
     * Fluent builder (SI units).
     * Keep any legacy constructors and delegate to this builder internally
     * to preserve binary compatibility.
     */
    public static final class Builder {
        private final String name;
        private double bodyRadius;              // m
        private double bodyMass;                // kg
        private double semiMajorAxis;           // m
        private double eccentricity;            // [0,1)
        private double inclinationRad;          // rad
        private double longAscNodeRad;          // rad
        private double argPeriapsisRad;         // rad
        private double meanLongitudeAtEpochRad; // rad
        private double siderealPeriod;          // s

        public Builder(String name) { this.name = Objects.requireNonNull(name, "name"); }

        public Builder bodyRadiusMeters(double v) { this.bodyRadius = v; return this; }
        public Builder bodyMassKg(double v) { this.bodyMass = v; return this; }
        public Builder semiMajorAxisMeters(double v) { this.semiMajorAxis = v; return this; }
        public Builder eccentricity(double v) { this.eccentricity = v; return this; }
        public Builder inclinationRadians(double v) { this.inclinationRad = v; return this; }
        public Builder longitudeAscendingNodeRadians(double v) { this.longAscNodeRad = v; return this; }
        public Builder argumentOfPeriapsisRadians(double v) { this.argPeriapsisRad = v; return this; }
        public Builder meanLongitudeAtEpochRadians(double v) { this.meanLongitudeAtEpochRad = v; return this; }
        public Builder siderealPeriodSeconds(double v) { this.siderealPeriod = v; return this; }

        public Moon build() { return new Moon(this); }
    }

    // ------------------------- Utils -------------------------

    private static double clamp01(double v) {
        if (Double.isNaN(v)) throw new IllegalArgumentException("eccentricity NaN");
        return Math.max(0.0, Math.min(0.999_999_999_999, v));
    }

    private static double positive(double v, String field) {
        if (!(v > 0.0) || Double.isNaN(v) || Double.isInfinite(v)) {
            throw new IllegalArgumentException(field + " must be finite and > 0 but was " + v);
        }
        return v;
    }

    private static double requireFinite(double v, String field) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            throw new IllegalArgumentException(field + " must be finite");
        }
        return v;
    }

    private static double normalizeAngle(double a) {
        final double twopi = Math.PI * 2.0;
        double r = a % twopi;
        if (r < 0) r += twopi;
        return r;
    }
}
