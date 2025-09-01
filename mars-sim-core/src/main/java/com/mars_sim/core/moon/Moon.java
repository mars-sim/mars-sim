package com.mars_sim.core.moon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.environment.PlanetaryEntity;

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
 * <p><b>Design goals</b>:
 * <ul>
 *   <li>Extends {@link PlanetaryEntity} so it is a {@link Unit} (required by the rest of the sim).</li>
 *   <li>Mutable POJO surface (no-arg constructor + setters) for existing loaders/serializers.</li>
 *   <li>Validation on write and angle normalization to avoid NaN/Inf propagation.</li>
 *   <li>Pure orbital helpers with no global singletons or clocks.</li>
 *   <li>Builder and copy/with helpers to support immutable-style use without breaking legacy code.</li>
 *   <li>Alias getters/setters (e.g., {@code getRadius()}, {@code setPeriod(double)}) to preserve older call sites.</li>
 * </ul>
 */
public class Moon extends PlanetaryEntity implements Serializable {

    /** Serialization ID for compatibility. */
    private static final long serialVersionUID = 1L;

    /** Default unit name used by the no-arg constructor. */
    private static final String DEFAULT_NAME = "Moon";

    // ---------------------------------------------------------------------
    // Physical/orbital properties (SI units)
    // ---------------------------------------------------------------------

    /** Physical mean radius (meters). For irregular bodies, use accepted mean radius. */
    private double bodyRadiusMeters;

    /** Mass (kilograms). */
    private double bodyMassKg;

    /** Semi-major axis (meters). */
    private double semiMajorAxisMeters;

    /** Eccentricity in [0, 1). */
    private double eccentricity;

    /** Inclination (radians). */
    private double inclinationRad;

    /** Longitude of ascending node, Ω (radians). */
    private double longitudeAscendingNodeRad;

    /** Argument of periapsis, ω (radians). */
    private double argumentOfPeriapsisRad;

    /**
     * Mean longitude at epoch, L₀ (radians).<br>
     * Note: L₀ = M₀ + ϖ, where ϖ = Ω + ω.
     */
    private double meanLongitudeAtEpochRad;

    /** Sidereal period, T (seconds). */
    private double siderealPeriodSeconds;

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    /**
     * No-arg constructor for serializers/DI; sets this unit's identity using the
     * default "Moon" name and {@link Unit#MOON_UNIT_ID}.
     *
     * <p>This ensures {@code Moon} is a proper {@link Unit} for APIs that expect Units.</p>
     */
    public Moon() {
        super(DEFAULT_NAME, Unit.MOON_UNIT_ID, UnitType.MOON);
    }

    /**
     * Full constructor with validation and normalization.
     * Also sets this unit's identity using {@code name} and {@link Unit#MOON_UNIT_ID}.
     *
     * @param name                           unique moon name
     * @param bodyRadiusMeters               radius in meters
     * @param bodyMassKg                     mass in kilograms
     * @param semiMajorAxisMeters            semi-major axis in meters
     * @param eccentricity                   eccentricity in [0,1)
     * @param inclinationRad                 inclination in radians
     * @param longitudeAscendingNodeRad      longitude of ascending node Ω in radians
     * @param argumentOfPeriapsisRad         argument of periapsis ω in radians
     * @param meanLongitudeAtEpochRad        mean longitude at epoch L₀ in radians
     * @param siderealPeriodSeconds          sidereal period in seconds
     */
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
        super(Objects.requireNonNull(name, "name"), Unit.MOON_UNIT_ID, UnitType.MOON);
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

    // ---------------------------------------------------------------------
    // Builder (encourages immutable-style construction in new code)
    // ---------------------------------------------------------------------

    /**
     * Create a builder for a new {@link Moon}.
     *
     * @param name unique unit name to assign to this Moon
     * @return builder
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /** Fluent builder (SI units). */
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

        /**
         * Construct a builder.
         *
         * @param name unique moon name
         */
        public Builder(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        /** @param v radius (m) */
        public Builder bodyRadiusMeters(double v) { this.bodyRadiusMeters = v; return this; }

        /** @param v mass (kg) */
        public Builder bodyMassKg(double v) { this.bodyMassKg = v; return this; }

        /** @param v semi-major axis (m) */
        public Builder semiMajorAxisMeters(double v) { this.semiMajorAxisMeters = v; return this; }

        /** @param v eccentricity in [0,1) */
        public Builder eccentricity(double v) { this.eccentricity = v; return this; }

        /** @param v inclination (rad) */
        public Builder inclinationRadians(double v) { this.inclinationRad = v; return this; }

        /** @param v longitude of ascending node Ω (rad) */
        public Builder longitudeAscendingNodeRadians(double v) { this.longitudeAscendingNodeRad = v; return this; }

        /** @param v argument of periapsis ω (rad) */
        public Builder argumentOfPeriapsisRadians(double v) { this.argumentOfPeriapsisRad = v; return this; }

        /** @param v mean longitude at epoch L₀ (rad) */
        public Builder meanLongitudeAtEpochRadians(double v) { this.meanLongitudeAtEpochRad = v; return this; }

        /** @param v sidereal period (s) */
        public Builder siderealPeriodSeconds(double v) { this.siderealPeriodSeconds = v; return this; }

        /**
         * Build the {@link Moon} instance with validated fields.
         *
         * @return constructed {@link Moon}
         */
        public Moon build() {
            return new Moon(
                    name,
                    bodyRadiusMeters,
                    bodyMassKg,
                    semiMajorAxisMeters,
                    eccentricity,
                    inclinationRad,
                    longitudeAscendingNodeRad,
                    argumentOfPeriapsisRad,
                    meanLongitudeAtEpochRad,
                    siderealPeriodSeconds
            );
        }
    }

    // ---------------------------------------------------------------------
    // Copy/with helpers (immutable-style updates, non-breaking)
    // ---------------------------------------------------------------------

    /** @return shallow copy (all fields are primitives or immutable) */
    public Moon copy() {
        return new Moon(
                getName(),
                bodyRadiusMeters,
                bodyMassKg,
                semiMajorAxisMeters,
                eccentricity,
                inclinationRad,
                longitudeAscendingNodeRad,
                argumentOfPeriapsisRad,
                meanLongitudeAtEpochRad,
                siderealPeriodSeconds
        );
    }

    /** @param v new unit name @return copied instance with updated name */
    public Moon withName(String v) {
        return new Moon(
                v,
                bodyRadiusMeters,
                bodyMassKg,
                semiMajorAxisMeters,
                eccentricity,
                inclinationRad,
                longitudeAscendingNodeRad,
                argumentOfPeriapsisRad,
                meanLongitudeAtEpochRad,
                siderealPeriodSeconds
        );
    }
    /** @param v new value @return copied instance with updated field */
    public Moon withBodyRadiusMeters(double v) { Moon m = copy(); m.setBodyRadiusMeters(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withBodyMassKg(double v) { Moon m = copy(); m.setBodyMassKg(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withSemiMajorAxisMeters(double v) { Moon m = copy(); m.setSemiMajorAxisMeters(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withEccentricity(double v) { Moon m = copy(); m.setEccentricity(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withInclinationRad(double v) { Moon m = copy(); m.setInclinationRad(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withLongitudeAscendingNodeRad(double v) { Moon m = copy(); m.setLongitudeAscendingNodeRad(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withArgumentOfPeriapsisRad(double v) { Moon m = copy(); m.setArgumentOfPeriapsisRad(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withMeanLongitudeAtEpochRad(double v) { Moon m = copy(); m.setMeanLongitudeAtEpochRad(v); return m; }
    /** @param v new value @return copied instance with updated field */
    public Moon withSiderealPeriodSeconds(double v) { Moon m = copy(); m.setSiderealPeriodSeconds(v); return m; }

    // ---------------------------------------------------------------------
    // Canonical getters (for the physical/orbital fields here)
    // NOTE: Unit identity/name are handled by PlanetaryEntity (Unit).
    // ---------------------------------------------------------------------

    /** @return body mean radius (m) */
    public double getBodyRadiusMeters() { return bodyRadiusMeters; }

    /** @return body mass (kg) */
    public double getBodyMassKg() { return bodyMassKg; }

    /** @return semi-major axis (m) */
    public double getSemiMajorAxisMeters() { return semiMajorAxisMeters; }

    /** @return eccentricity in [0,1) */
    public double getEccentricity() { return eccentricity; }

    /** @return inclination (rad) */
    public double getInclinationRad() { return inclinationRad; }

    /** @return longitude of ascending node Ω (rad) */
    public double getLongitudeAscendingNodeRad() { return longitudeAscendingNodeRad; }

    /** @return argument of periapsis ω (rad) */
    public double getArgumentOfPeriapsisRad() { return argumentOfPeriapsisRad; }

    /** @return mean longitude at epoch L₀ (rad) */
    public double getMeanLongitudeAtEpochRad() { return meanLongitudeAtEpochRad; }

    /** @return sidereal period (s) */
    public double getSiderealPeriodSeconds() { return siderealPeriodSeconds; }

    // ---------------------------------------------------------------------
    // Legacy alias getters (compatibility)
    // ---------------------------------------------------------------------

    /** Alias of {@link #getBodyRadiusMeters()}. @return radius (m) */
    public double getRadius() { return getBodyRadiusMeters(); }

    /** Alias of {@link #getInclinationRad()}. @return inclination (rad) */
    public double getInclination() { return getInclinationRad(); }

    /** Alias of {@link #getSemiMajorAxisMeters()}. @return semi-major axis (m) */
    public double getSemiMajorAxis() { return getSemiMajorAxisMeters(); }

    /** Alias of {@link #getSiderealPeriodSeconds()}. @return period (s) */
    public double getPeriod() { return getSiderealPeriodSeconds(); }

    /** Alias of {@link #getBodyMassKg()}. @return mass (kg) */
    public double getMass() { return getBodyMassKg(); }

    /** Alias of {@link #getLongitudeAscendingNodeRad()}. @return Ω (rad) */
    public double getLongitudeAscendingNode() { return getLongitudeAscendingNodeRad(); }

    /** Alias of {@link #getArgumentOfPeriapsisRad()}. @return ω (rad) */
    public double getArgumentOfPeriapsis() { return getArgumentOfPeriapsisRad(); }

    /** Alias of {@link #getMeanLongitudeAtEpochRad()}. @return L₀ (rad) */
    public double getMeanLongitudeAtEpoch() { return getMeanLongitudeAtEpochRad(); }

    // ---------------------------------------------------------------------
    // Validating setters for the physical/orbital fields
    // ---------------------------------------------------------------------

    /**
     * Set body radius.
     * @param v meters, must be &gt; 0 and finite
     */
    public void setBodyRadiusMeters(double v) {
        this.bodyRadiusMeters = requirePositiveFinite(v, "bodyRadiusMeters");
    }

    /**
     * Set body mass.
     * @param v kilograms, must be &gt; 0 and finite
     */
    public void setBodyMassKg(double v) {
        this.bodyMassKg = requirePositiveFinite(v, "bodyMassKg");
    }

    /**
     * Set semi-major axis.
     * @param v meters, must be &gt; 0 and finite
     */
    public void setSemiMajorAxisMeters(double v) {
        this.semiMajorAxisMeters = requirePositiveFinite(v, "semiMajorAxisMeters");
    }

    /**
     * Set eccentricity.
     * @param v eccentricity in [0,1)
     */
    public void setEccentricity(double v) {
        if (Double.isNaN(v) || v < 0.0 || v >= 1.0) {
            throw new IllegalArgumentException("eccentricity must be in [0,1): " + v);
        }
        this.eccentricity = v;
    }

    /**
     * Set inclination.
     * @param v radians, finite
     */
    public void setInclinationRad(double v) {
        this.inclinationRad = requireFinite(v, "inclinationRad");
    }

    /**
     * Set longitude of ascending node, Ω.
     * @param v radians, finite; normalized to [0, 2π)
     */
    public void setLongitudeAscendingNodeRad(double v) {
        this.longitudeAscendingNodeRad = normalizeAngleRad(requireFinite(v, "longitudeAscendingNodeRad"));
    }

    /**
     * Set argument of periapsis, ω.
     * @param v radians, finite; normalized to [0, 2π)
     */
    public void setArgumentOfPeriapsisRad(double v) {
        this.argumentOfPeriapsisRad = normalizeAngleRad(requireFinite(v, "argumentOfPeriapsisRad"));
    }

    /**
     * Set mean longitude at epoch, L₀.
     * @param v radians, finite; normalized to [0, 2π)
     */
    public void setMeanLongitudeAtEpochRad(double v) {
        this.meanLongitudeAtEpochRad = normalizeAngleRad(requireFinite(v, "meanLongitudeAtEpochRad"));
    }

    /**
     * Set sidereal period.
     * @param v seconds, must be &gt; 0 and finite
     */
    public void setSiderealPeriodSeconds(double v) {
        this.siderealPeriodSeconds = requirePositiveFinite(v, "siderealPeriodSeconds");
    }

    // ---------------------------------------------------------------------
    // Legacy alias setters (compatibility)
    // ---------------------------------------------------------------------

    /** Alias of {@link #setBodyRadiusMeters(double)}. */
    public void setRadius(double v) { setBodyRadiusMeters(v); }

    /** Alias of {@link #setInclinationRad(double)}. */
    public void setInclination(double v) { setInclinationRad(v); }

    /** Alias of {@link #setSemiMajorAxisMeters(double)}. */
    public void setSemiMajorAxis(double v) { setSemiMajorAxisMeters(v); }

    /** Alias of {@link #setSiderealPeriodSeconds(double)}. */
    public void setPeriod(double v) { setSiderealPeriodSeconds(v); }

    /** Alias of {@link #setBodyMassKg(double)}. */
    public void setMass(double v) { setBodyMassKg(v); }

    /** Alias of {@link #setLongitudeAscendingNodeRad(double)}. */
    public void setLongitudeAscendingNode(double v) { setLongitudeAscendingNodeRad(v); }

    /** Alias of {@link #setArgumentOfPeriapsisRad(double)}. */
    public void setArgumentOfPeriapsis(double v) { setArgumentOfPeriapsisRad(v); }

    /** Alias of {@link #setMeanLongitudeAtEpochRad(double)}. */
    public void setMeanLongitudeAtEpoch(double v) { setMeanLongitudeAtEpochRad(v); }

    // ---------------------------------------------------------------------
    // Orbital helpers (pure; no global state)
    // ---------------------------------------------------------------------

    /**
     * Mean motion n (radians/second).
     * @return 2π / T
     */
    public double getMeanMotionRadPerSec() {
        return (2.0 * Math.PI) / siderealPeriodSeconds;
    }

    /**
     * Legacy alias for mean motion.
     * @return mean motion (radians/second)
     */
    public double getMeanMotion() { return getMeanMotionRadPerSec(); }

    /**
     * Mean anomaly M(t) at elapsed time since epoch.
     * <p>M = L - ϖ + n·Δt, where ϖ = Ω + ω.</p>
     *
     * @param secondsSinceEpoch Δt in seconds
     * @return mean anomaly in radians normalized to [0, 2π)
     */
    public double meanAnomalyAtSecondsSinceEpoch(double secondsSinceEpoch) {
        final double L = meanLongitudeAtEpochRad + getMeanMotionRadPerSec() * secondsSinceEpoch;
        final double varpi = longitudeAscendingNodeRad + argumentOfPeriapsisRad;
        return normalizeAngleRad(L - varpi);
    }

    /**
     * Eccentric anomaly E from mean anomaly M via Newton iterations.
     *
     * @param meanAnomalyRad mean anomaly (radians)
     * @return eccentric anomaly (radians) normalized to [0, 2π)
     */
    public double eccentricAnomalyFromMeanAnomaly(double meanAnomalyRad) {
        final double e = eccentricity;
        if (e == 0.0) return normalizeAngleRad(meanAnomalyRad);
        double E = meanAnomalyRad; // initial guess
        for (int i = 0; i < 8; i++) {
            final double f = E - e * Math.sin(E) - meanAnomalyRad;
            final double fp = 1.0 - e * Math.cos(E);
            E -= f / fp;
        }
        return normalizeAngleRad(E);
    }

    /**
     * True anomaly ν from eccentric anomaly E.
     *
     * @param eccentricAnomalyRad eccentric anomaly (radians)
     * @return true anomaly (radians)
     */
    public double trueAnomalyFromE(double eccentricAnomalyRad) {
        final double e = eccentricity;
        final double cosE = Math.cos(eccentricAnomalyRad);
               final double sinE = Math.sin(eccentricAnomalyRad);
        final double denom = 1.0 - e * cosE;
        final double cosV = (cosE - e) / denom;
        final double sinV = Math.sqrt(1.0 - e * e) * sinE / denom;
        return Math.atan2(sinV, cosV);
    }

    /**
     * Orbital radius r at eccentric anomaly E.
     *
     * @param eccentricAnomalyRad eccentric anomaly (radians)
     * @return radius (meters)
     */
    public double radiusAtE(double eccentricAnomalyRad) {
        return semiMajorAxisMeters * (1.0 - eccentricity * Math.cos(eccentricAnomalyRad));
    }

    /**
     * Position in the parent-body equatorial frame (meters).
     * Rotation order: Rz(Ω) · Rx(i) · Rz(ω).
     *
     * @param epoch reference epoch t0
     * @param when  absolute time to evaluate
     * @return array {x, y, z} in meters
     * @throws NullPointerException if epoch or when is null
     */
    public double[] positionAt(Instant epoch, Instant when) {
        Objects.requireNonNull(epoch, "epoch");
        Objects.requireNonNull(when, "when");

        final long dtSeconds = Duration.between(epoch, when).getSeconds();
        final double dt = (double) dtSeconds;

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

    /**
     * Convenience overload that evaluates position at a given Δt seconds past epoch.
     *
     * @param epoch             reference epoch t0
     * @param secondsSinceEpoch Δt in seconds
     * @return array {x, y, z} in meters
     * @throws NullPointerException if epoch is null
     */
    public double[] positionAtSecondsSinceEpoch(Instant epoch, double secondsSinceEpoch) {
        Objects.requireNonNull(epoch, "epoch");
        final long whole = (long) Math.floor(secondsSinceEpoch);
        return positionAt(epoch, epoch.plusSeconds(whole));
    }

    // ---------------------------------------------------------------------
    // Value semantics & debug
    // ---------------------------------------------------------------------

    /**
     * Two {@code Moon} objects are considered equal if their names are equal
     * (names are expected to be unique identifiers in the simulation).
     *
     * @param other other object
     * @return true if equal by name
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Moon)) return false;
        Moon m = (Moon) other;
        return Objects.equals(this.getName(), m.getName());
    }

    /** @return hash code based on {@code name} (may be 0 if name is null) */
    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    /** @return concise debug string */
    @Override
    public String toString() {
        return "Moon[" + getName() + "]";
    }

    // ---------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------

    /**
     * Normalize an angle to the range [0, 2π).
     *
     * @param a angle (radians)
     * @return normalized angle (radians)
     */
    public static double normalizeAngleRad(double a) {
        final double tau = Math.PI * 2.0;
        double r = a % tau;
        if (r < 0) r += tau;
        return r;
    }

    /**
     * Ensure a value is finite and &gt; 0.
     *
     * @param v     value
     * @param field field name
     * @return the same value if valid
     * @throws IllegalArgumentException if not finite or not &gt; 0
     */
    private static double requirePositiveFinite(double v, String field) {
        if (!(v > 0.0) || Double.isNaN(v) || Double.isInfinite(v)) {
            throw new IllegalArgumentException(field + " must be finite and > 0 but was " + v);
        }
        return v;
    }

    /**
     * Ensure a value is finite (not NaN or infinite).
     *
     * @param v     value
     * @param field field name
     * @return the same value if valid
     * @throws IllegalArgumentException if NaN or infinite
     */
    private static double requireFinite(double v, String field) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            throw new IllegalArgumentException(field + " must be finite");
        }
        return v;
    }

    // ---------------------------------------------------------------------
    // Convenience presets (optional; safe to ignore in existing code)
    // NOTE: These create Moon Units with the same Unit ID; they are intended
    //       for testing/demonstration only and should not be registered into
    //       the global Unit registry alongside an existing Moon instance.
    // ---------------------------------------------------------------------

    /**
     * Create a Moon preset for Phobos (Mars I).
     * Values are approximate; callers can override with epoch-specific elements as desired.
     *
     * @return {@link Moon} instance for Phobos (Unit identity will use {@link Unit#MOON_UNIT_ID})
     */
    public static Moon phobos() {
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

    /**
     * Create a Moon preset for Deimos (Mars II).
     * Values are approximate; callers can override with epoch-specific elements as desired.
     *
     * @return {@link Moon} instance for Deimos (Unit identity will use {@link Unit#MOON_UNIT_ID})
     */
    public static Moon deimos() {
        return Moon.builder("Deimos")
                .bodyRadiusMeters(6_270.0)
                .bodyMassKg(1.51e15)
                .semiMajorAxisMeters(23_463_200.0)
                .eccentricity(0.00033)
                .inclinationRadians(Math.toRadians(0.93))
                .longitudeAscendingNodeRadians(0.0)
                .argumentOfPeriapsisRadians(0.0)
                .meanLongitudeAtEpochRadians(0.0)
                .siderealPeriodSeconds((long) Math.round(30.312 * 3600.0))
                .build();
    }
}
