package com.mars_sim.core.moon;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Describes a natural satellite ("moon") used by the simulation.
 *
 * <p><b>Units</b>:
 * <ul>
 *   <li>Length: meters (m)</li>
 *   <li>Mass: kilograms (kg)</li>
 *   <li>Angle: radians (rad)</li>
 *   <li>Time: seconds (s)</li>
 * </ul>
 *
 * <p><b>Design (interop‑friendly):</b>
 * <ul>
 *   <li>Mutable POJO (to avoid breaking legacy callers that set fields)</li>
 *   <li>Validation on writes + normalizing helpers</li>
 *   <li>Pure orbital math helpers with no global clocks/singletons</li>
 *   <li>Builder + copy methods to encourage new code to go immutable</li>
 *   <li>Alias getters/setters to keep ambiguous legacy names working</li>
 * </ul>
 */
public class Moon implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    // ----------------------------
    // Identity & bulk properties
    // ----------------------------

    /** Unique name (e.g., "Phobos", "Deimos"). */
    private volatile String name;

    /** Physical mean radius [m]. For irregular bodies, use the accepted mean radius. */
    private volatile double bodyRadiusMeters;

    /** Mass [kg]. */
    private volatile double bodyMassKg;

    // ----------------------------
    // Keplerian elements (parent-body equatorial frame)
    // ----------------------------

    /** Semi-major axis [m]. */
    private volatile double semiMajorAxisMeters;

    /** Eccentricity [0, 1). */
    private volatile double eccentricity;

    /** Inclination [rad]. */
    private volatile double inclinationRad;

    /** Longitude of ascending node Ω [rad]. */
    private volatile double longitudeAscendingNodeRad;

    /** Argument of periapsis ω [rad]. */
    private volatile double argumentOfPeriapsisRad;

    /** Mean longitude at epoch L0 [rad] (i.e., M0 + ϖ). */
    private volatile double meanLongitudeAtEpochRad;

    /** Sidereal period T [s]. */
    private volatile double siderealPeriodSeconds;

    // ----------------------------
    // Constructors
    // ----------------------------

    /** No-arg ctor for serializers/DI; fields should be set via setters or builder. */
    public Moon() {}

    /** Full constructor (fields are validated and normalized). */
    public Moon(
            String name,
            double bodyRadiusMeters,
            double bodyMassKg,
            double semiMajorAxisMeters,
            double eccentricity,
            double inclinationRad,
            double longitudeAscendingNodeRad,
            double argumentOfPeriapsisRad,
            double meanLongitudeAtEpochRad,
            double siderealPeriodSeconds
    ) {
        setName(name);
        setBodyRadiusMeters(bodyRadiusMeters);
        setBodyMassKg(bodyMassKg);
        setSemiMajorAxisMeters(semiMajorAxisMeters);
        setEccentricity(eccentricity);
        setInclinationRad(inclinationRad);
        setLongitudeAscendingNodeRad(longitudeAscendingNodeRad);
        setArgumentOfPeriapsisRad(argumentOfPeriapsisRad);
        setMeanLongitudeAtEpochRad(meanLongitudeAtEpochRad);
        setSiderealPeriodSeconds(siderealPeriodSeconds);
    }

    // ----------------------------
    // Builder (encourages immutable style in new code)
    // ----------------------------

    public static Builder builder(String name) { return new Builder(name); }

    public static final class Builder {
        private final String name;
        private double bodyRadiusMeters;
        private double bodyMassKg;
        private double semiMajorAxisMeters;
        private double eccentricity;
        private double inclinationRad;
        private double longitudeAscendingNodeRad;
        private double argumentOfPeriapsisRad;
        private double meanLongitudeAtEpochRad;
        private double siderealPeriodSeconds;

        public Builder(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        public Builder bodyRadiusMeters(double v) { this.bodyRadiusMeters = v; return this; }
        public Builder bodyMassKg(double v) { this.bodyMassKg = v; return this; }
        public Builder semiMajorAxisMeters(double v) { this.semiMajorAxisMeters = v; return this; }
        public Builder eccentricity(double v) { this.eccentricity = v; return this; }
        public Builder inclinationRadians(double v) { this.inclinationRad = v; return this; }
        public Builder longitudeAscendingNodeRadians(double v) { this.longitudeAscendingNodeRad = v; return this; }
        public Builder argumentOfPeriapsisRadians(double v) { this.argumentOfPeriapsisRad = v; return this; }
        public Builder meanLongitudeAtEpochRadians(double v) { this.meanLongitudeAtEpochRad = v; return this; }
        public Builder siderealPeriodSeconds(double v) { this.siderealPeriodSeconds = v; return this; }

        public Moon build() {
            return new Moon(
                    name, bodyRadiusMeters, bodyMassKg, semiMajorAxisMeters, eccentricity,
                    inclinationRad, longitudeAscendingNodeRad, argumentOfPeriapsisRad,
                    meanLongitudeAtEpochRad, siderealPeriodSeconds
            );
        }
    }

    // ----------------------------
    // Copy/with helpers (immutable-style updates)
    // ----------------------------

    public Moon withName(String v) { Moon m = this.copy(); m.setName(v); return m; }
    public Moon withBodyRadiusMeters(double v) { Moon m = this.copy(); m.setBodyRadiusMeters(v); return m; }
    public Moon withBodyMassKg(double v) { Moon m = this.copy(); m.setBodyMassKg(v); return m; }
    public Moon withSemiMajorAxisMeters(double v) { Moon m = this.copy(); m.setSemiMajorAxisMeters(v); return m; }
    public Moon withEccentricity(double v) { Moon m = this.copy(); m.setEccentricity(v); return m; }
    public Moon withInclinationRad(double v) { Moon m = this.copy(); m.setInclinationRad(v); return m; }
    public Moon withLongitudeAscendingNodeRad(double v) { Moon m = this.copy(); m.setLongitudeAscendingNodeRad(v); return m; }
    public Moon withArgumentOfPeriapsisRad(double v) { Moon m = this.copy(); m.setArgumentOfPeriapsisRad(v); return m; }
    public Moon withMeanLongitudeAtEpochRad(double v) { Moon m = this.copy(); m.setMeanLongitudeAtEpochRad(v); return m; }
    public Moon withSiderealPeriodSeconds(double v) { Moon m = this.copy(); m.setSiderealPeriodSeconds(v); return m; }

    /** Shallow copy (all fields are primitives/immutable). */
    public Moon copy() {
        return new Moon(
                name, bodyRadiusMeters, bodyMassKg, semiMajorAxisMeters, eccentricity,
                inclinationRad, longitudeAscendingNodeRad, argumentOfPeriapsisRad,
                meanLongitudeAtEpochRad, siderealPeriodSeconds
        );
    }

    // ----------------------------
    // Getters (canonical)
    // ----------------------------

    public String getName() { return name; }
    public double getBodyRadiusMeters() { return bodyRadiusMeters; }
    public double getBodyMassKg() { return bodyMassKg; }
    public double getSemiMajorAxisMeters() { return semiMajorAxisMeters; }
    public double getEccentricity() { return eccentricity; }
    public double getInclinationRad() { return inclinationRad; }
    public double getLongitudeAscendingNodeRad() { return longitudeAscendingNodeRad; }
    public double getArgumentOfPeriapsisRad() { return argumentOfPeriapsisRad; }
    public double getMeanLongitudeAtEpochRad() { return meanLongitudeAtEpochRad; }
    public double getSiderealPeriodSeconds() { return siderealPeriodSeconds; }

    // ----------------------------
    // Legacy alias getters (keep old code compiling)
    // ----------------------------

    /** Alias for {@link #getBodyRadiusMeters()} (unit explicitly meters). */
    public double getRadius() { return getBodyRadiusMeters(); }

    /** Alias for {@link #getInclinationRad()} (unit explicitly radians). */
    public double getInclination() { return getInclinationRad(); }

    /** Alias for {@link #getSemiMajorAxisMeters()} (unit explicitly meters). */
    public double getSemiMajorAxis() { return getSemiMajorAxisMeters(); }

    /** Alias for {@link #getSiderealPeriodSeconds()} (unit explicitly seconds). */
    public double getPeriod() { return getSiderealPeriodSeconds(); }

    // ----------------------------
    // Setters (validated & normalized)
    // ----------------------------

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public void setBodyRadiusMeters(double v) {
        this.bodyRadiusMeters = requirePositiveFinite(v, "bodyRadiusMeters");
    }

    public void setBodyMassKg(double v) {
        this.bodyMassKg = requirePositiveFinite(v, "bodyMassKg");
    }

    public void setSemiMajorAxisMeters(double v) {
        this.semiMajorAxisMeters = requirePositiveFinite(v, "semiMajorAxisMeters");
    }

    public void setEccentricity(double v) {
        if (Double.isNaN(v) || v < 0.0 || v >= 1.0) {
            throw new IllegalArgumentException("eccentricity must be in [0,1): " + v);
        }
        this.eccentricity = v;
    }

    public void setInclinationRad(double v) {
        this.inclinationRad = requireFinite(v, "inclinationRad");
    }

    public void setLongitudeAscendingNodeRad(double v) {
        this.longitudeAscendingNodeRad = normalizeAngleRad(requireFinite(v, "longitudeAscendingNodeRad"));
    }

    public void setArgumentOfPeriapsisRad(double v) {
        this.argumentOfPeriapsisRad = normalizeAngleRad(requireFinite(v, "argumentOfPeriapsisRad"));
    }

    public void setMeanLongitudeAtEpochRad(double v) {
        this.meanLongitudeAtEpochRad = normalizeAngleRad(requireFinite(v, "meanLongitudeAtEpochRad"));
    }

    public void setSiderealPeriodSeconds(double v) {
        this.siderealPeriodSeconds = requirePositiveFinite(v, "siderealPeriodSeconds");
    }

    // ----------------------------
    // Legacy alias setters (avoid breakage)
    // ----------------------------

    /** Alias of {@link #setBodyRadiusMeters(double)}. */
    public void setRadius(double v) { setBodyRadiusMeters(v); }

    /** Alias of {@link #setInclinationRad(double)}. */
    public void setInclination(double v) { setInclinationRad(v); }

    /** Alias of {@link #setSemiMajorAxisMeters(double)}. */
    public void setSemiMajorAxis(double v) { setSemiMajorAxisMeters(v); }

    /** Alias of {@link #setSiderealPeriodSeconds(double)}. */
    public void setPeriod(double v) { setSiderealPeriodSeconds(v); }

    // ----------------------------
    // Orbital helpers (pure functions; no global state)
    // ----------------------------

    /** Mean motion n [rad/s]. */
    public double getMeanMotionRadPerSec() {
        return (2.0 * Math.PI) / siderealPeriodSeconds;
    }

    /**
     * Mean anomaly M(t) [rad] at elapsed seconds since epoch.
     * M = L - ϖ + n * Δt, where ϖ = Ω + ω.
     */
    public double meanAnomalyAtSecondsSinceEpoch(double secondsSinceEpoch) {
        final double L = meanLongitudeAtEpochRad + getMeanMotionRadPerSec() * secondsSinceEpoch;
        final double varpi = longitudeAscendingNodeRad + argumentOfPeriapsisRad; // ϖ
        return normalizeAngleRad(L - varpi);
    }

    /** Eccentric anomaly E [rad] from mean anomaly M [rad] via Newton iterations. */
    public double eccentricAnomalyFromMeanAnomaly(double meanAnomalyRad) {
        final double e = eccentricity;
        if (e == 0.0) return normalizeAngleRad(meanAnomalyRad);
        double E = meanAnomalyRad; // initial guess
        for (int i = 0; i < 8; i++) {
            double f = E - e * Math.sin(E) - meanAnomalyRad;
            double fp = 1.0 - e * Math.cos(E);
            E -= f / fp;
        }
        return normalizeAngleRad(E);
    }

    /** True anomaly ν [rad] from eccentric anomaly E [rad]. */
    public double trueAnomalyFromE(double eccentricAnomalyRad) {
        final double e = eccentricity;
        final double cosE = Math.cos(eccentricAnomalyRad);
        final double sinE = Math.sin(eccentricAnomalyRad);
        final double denom = 1.0 - e * cosE;
        final double cosV = (cosE - e) / denom;
        final double sinV = Math.sqrt(1.0 - e * e) * sinE / denom;
        return Math.atan2(sinV, cosV);
    }

    /** Radius r [m] at eccentric anomaly E [rad]. */
    public double radiusAtE(double eccentricAnomalyRad) {
        return semiMajorAxisMeters * (1.0 - eccentricity * Math.cos(eccentricAnomalyRad));
    }

    /**
     * Position in parent-body equatorial frame (meters).
     * Rotation order: Rz(Ω) · Rx(i) · Rz(ω).
     *
     * @param epoch reference epoch t0
     * @param when absolute time to evaluate
     * @return double[3] = {x, y, z} in meters
     */
    public double[] positionAt(Instant epoch, Instant when) {
        Objects.requireNonNull(epoch, "epoch");
        Objects.requireNonNull(when, "when");

        final double dt = Duration.between(epoch, when).toSeconds();
        final double M  = meanAnomalyAtSecondsSinceEpoch(dt);
        final double E  = eccentricAnomalyFromMeanAnomaly(M);
        final double v  = trueAnomalyFromE(E);
        final double r  = radiusAtE(E);

        // 2D in orbital plane:
        final double xOrb = r * Math.cos(v);
        final double yOrb = r * Math.sin(v);

        // rotate by ω, i, Ω
        final double cosO = Math.cos(longitudeAscendingNodeRad), sinO = Math.sin(longitudeAscendingNodeRad);
        final double cosI = Math.cos(inclinationRad),          sinI = Math.sin(inclinationRad);
        final double cosw = Math.cos(argumentOfPeriapsisRad),  sinw = Math.sin(argumentOfPeriapsisRad);

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

    // ----------------------------
    // Value semantics & debug
    // ----------------------------

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Moon m)) return false;
        return Objects.equals(this.name, m.name);
    }

    @Override public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override public String toString() {
        return "Moon[" + name + "]";
    }

    // ----------------------------
    // Utility
    // ----------------------------

    private static double normalizeAngleRad(double a) {
        final double tau = Math.PI * 2.0;
        double r = a % tau;
        if (r < 0) r += tau;
        return r;
    }

    private static double requirePositiveFinite(double v, String field) {
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

    // ----------------------------
    // Convenience: well-known moons (optional)
    // These helpers introduce no coupling and can be ignored by existing code.
    // Sources: NASA factsheets + Wikipedia (for mean radius/mass and orbital elements).
    // ----------------------------

    /** Create a Moon preset for Phobos (Mars I). */
    public static Moon phobos() {
        // Semimajor axis ~ 9,376 km; e ~ 0.0151; i ~ 1.093° to Mars equator;
        // Period ~ 7h 39m 12s; mean radius ~ 11.08 km; mass ~ 1.06e16 kg.
        // Angles not strictly published here; safe defaults are 0 and can be tuned by content loaders.
        return Moon.builder("Phobos")
                .bodyRadiusMeters(11_080.0)
                .bodyMassKg(1.06e16)
                .semiMajorAxisMeters(9_376_000.0)
                .eccentricity(0.0151)
                .inclinationRadians(Math.toRadians(1.093))
                .longitudeAscendingNodeRadians(0.0)
                .argumentOfPeriapsisRadians(0.0)
                .meanLongitudeAtEpochRadians(0.0)
                .siderealPeriodSeconds((7 * 3600.0) + (39 * 60.0) + 12.0)
                .build();
    }

    /** Create a Moon preset for Deimos (Mars II). */
    public static Moon deimos() {
        // Semimajor axis ~ 23,463.2 km; e ~ 0.00033; i ~ 0.93°; Period ~ 30.312 h;
        // mean radius ~ 6.27 km; mass ~ 1.51e15 kg.
        return Moon.builder("Deimos")
                .bodyRadiusMeters(6_270.0)
                .bodyMassKg(1.51e15)
                .semiMajorAxisMeters(23_463_200.0)
                .eccentricity(0.00033)
                .inclinationRadians(Math.toRadians(0.93))
                .longitudeAscendingNodeRadians(0.0)
                .argumentOfPeriapsisRadians(0.0)
                .meanLongitudeAtEpochRadians(0.0)
                .siderealPeriodSeconds(30.312 * 3600.0)
                .build();
    }
}
