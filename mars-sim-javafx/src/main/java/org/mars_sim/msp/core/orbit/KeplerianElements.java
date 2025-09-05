/*
 * This file is part of the Mars Simulation Project (mars-sim).
 * License: GPL-3.0 (see the project root LICENSE file).
 *
 * Purpose:
 *   Immutable container and utilities for classical Keplerian orbital
 *   elements referenced to Mars (areocentric frame).
 *
 * Elements:
 *   - a  : semi-major axis [meters]
 *   - e  : eccentricity [-] (0 ≤ e < 1 for elliptical orbits)
 *   - i  : inclination [radians]
 *   - Ω  : right ascension of ascending node (RAAN) [radians]
 *   - ω  : argument of periapsis [radians]
 *   - M0 : mean anomaly at epoch [radians]
 *   - t0 : epoch (UTC, java.time.Instant)
 *
 * Conventions & Notes:
 *   - Angles are stored in radians. Use the factory methods that accept degrees
 *     if you prefer degrees on input.
 *   - On construction, angles are normalized:
 *        RAAN, ω, M0 ∈ [0, 2π)
 *     Inclination is normalized to [0, π]. If i > π, we map:
 *        i' = 2π - i,  Ω' = Ω + π,  ω' = ω + π   (mod 2π)
 *     which preserves the orbit geometry.
 *   - The mean motion/period functions default to Mars μ from AreoBodyConstants.
 *   - This class intentionally avoids any external dependencies and can be used
 *     by both a simple two-body propagator and higher-order models.
 */

package com.mars_sim.core.orbit;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public final class KeplerianElements {

    // ---------------------------------------------------------------------
    // Fields (immutable)
    // ---------------------------------------------------------------------

    private final double semiMajorAxisM;
    private final double eccentricity;
    private final double inclinationRad;
    private final double raanRad;               // Ω
    private final double argOfPeriapsisRad;     // ω
    private final double meanAnomalyAtEpochRad; // M0
    private final Instant epoch;                // t0

    // ---------------------------------------------------------------------
    // Construction
    // ---------------------------------------------------------------------

    private KeplerianElements(
            double semiMajorAxisM,
            double eccentricity,
            double inclinationRad,
            double raanRad,
            double argOfPeriapsisRad,
            double meanAnomalyAtEpochRad,
            Instant epoch) {

        if (!(semiMajorAxisM > 0.0) || !Double.isFinite(semiMajorAxisM)) {
            throw new IllegalArgumentException("Semi-major axis must be finite and > 0 (meters).");
        }
        if (!(eccentricity >= 0.0) || !(eccentricity < 1.0) || !Double.isFinite(eccentricity)) {
            throw new IllegalArgumentException("Eccentricity must satisfy 0 ≤ e < 1.");
        }
        Objects.requireNonNull(epoch, "epoch must not be null");

        // Normalize angles
        double i = wrapToTwoPi(inclinationRad);
        double Ω = wrapToTwoPi(raanRad);
        double ω = wrapToTwoPi(argOfPeriapsisRad);
        double M0 = wrapToTwoPi(meanAnomalyAtEpochRad);

        // Keep inclination in [0, π]; adjust node/periapsis if > π.
        if (i > Math.PI) {
            i = 2.0 * Math.PI - i;
            Ω = wrapToTwoPi(Ω + Math.PI);
            ω = wrapToTwoPi(ω + Math.PI);
        }

        this.semiMajorAxisM = semiMajorAxisM;
        this.eccentricity = eccentricity;
        this.inclinationRad = i;
        this.raanRad = Ω;
        this.argOfPeriapsisRad = ω;
        this.meanAnomalyAtEpochRad = M0;
        this.epoch = epoch;
    }

    /** Factory: inputs in meters/radians. */
    public static KeplerianElements ofMetersRadians(
            double semiMajorAxisM,
            double eccentricity,
            double inclinationRad,
            double raanRad,
            double argOfPeriapsisRad,
            double meanAnomalyAtEpochRad,
            Instant epoch) {
        return new KeplerianElements(semiMajorAxisM, eccentricity, inclinationRad, raanRad, argOfPeriapsisRad, meanAnomalyAtEpochRad, epoch);
    }

    /** Factory: inputs in kilometers/degrees. */
    public static KeplerianElements ofKilometersDegrees(
            double semiMajorAxisKm,
            double eccentricity,
            double inclinationDeg,
            double raanDeg,
            double argOfPeriapsisDeg,
            double meanAnomalyAtEpochDeg,
            Instant epoch) {
        return new KeplerianElements(
                semiMajorAxisKm * 1_000.0,
                eccentricity,
                Math.toRadians(inclinationDeg),
                Math.toRadians(raanDeg),
                Math.toRadians(argOfPeriapsisDeg),
                Math.toRadians(meanAnomalyAtEpochDeg),
                epoch);
    }

    /**
     * Convenience: ideal areostationary elements (circular equatorial), RAAN/ω/M0 = 0.
     * Epoch default is AreoBodyConstants.DEFAULT_EPOCH unless provided.
     */
    public static KeplerianElements areostationary() {
        return areostationary(AreoBodyConstants.DEFAULT_EPOCH);
    }

    public static KeplerianElements areostationary(Instant epoch) {
        return ofMetersRadians(
                AreoBodyConstants.AREOSTATIONARY_SEMIMAJOR_AXIS_M,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                epoch);
    }

    /**
     * Factory from mean motion n [rad/s] (elliptical, two-body): a = cbrt( μ / n^2 ).
     * Uses Mars μ from AreoBodyConstants.
     */
    public static KeplerianElements ofMeanMotion(
            double meanMotionRadPerSec,
            double eccentricity,
            double inclinationRad,
            double raanRad,
            double argOfPeriapsisRad,
            double meanAnomalyAtEpochRad,
            Instant epoch) {
        if (!(meanMotionRadPerSec > 0.0) || !Double.isFinite(meanMotionRadPerSec)) {
            throw new IllegalArgumentException("Mean motion must be finite and > 0 [rad/s].");
        }
        double a = Math.cbrt(AreoBodyConstants.MARS_GM_M3_S2 / (meanMotionRadPerSec * meanMotionRadPerSec));
        return ofMetersRadians(a, eccentricity, inclinationRad, raanRad, argOfPeriapsisRad, meanAnomalyAtEpochRad, epoch);
    }

    // ---------------------------------------------------------------------
    // Basic accessors
    // ---------------------------------------------------------------------

    public double getSemiMajorAxisM() {
        return semiMajorAxisM;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getInclinationRad() {
        return inclinationRad;
    }

    public double getRaanRad() {
        return raanRad;
    }

    public double getArgOfPeriapsisRad() {
        return argOfPeriapsisRad;
    }

    public double getMeanAnomalyAtEpochRad() {
        return meanAnomalyAtEpochRad;
    }

    public Instant getEpoch() {
        return epoch;
    }

    // ---------------------------------------------------------------------
    // Derived orbital quantities (two-body)
    // ---------------------------------------------------------------------

    /** Mean motion n [rad/s] using Mars μ. */
    public double meanMotionRadPerSec() {
        return Math.sqrt(AreoBodyConstants.MARS_GM_M3_S2 / (semiMajorAxisM * semiMajorAxisM * semiMajorAxisM));
    }

    /** Orbital period T [s] using Mars μ. */
    public double periodSeconds() {
        return AreoBodyConstants.TWO_PI / meanMotionRadPerSec();
    }

    /** Semi-latus rectum p [m] = a (1 - e^2). */
    public double semiLatusRectumM() {
        return semiMajorAxisM * (1.0 - eccentricity * eccentricity);
    }

    /** Periapsis radius r_p [m] = a (1 - e). */
    public double periapsisRadiusM() {
        return semiMajorAxisM * (1.0 - eccentricity);
    }

    /** Apoapsis radius r_a [m] = a (1 + e). */
    public double apoapsisRadiusM() {
        return semiMajorAxisM * (1.0 + eccentricity);
    }

    // ---------------------------------------------------------------------
    // Time & anomaly helpers
    // ---------------------------------------------------------------------

    /** Seconds (double) from epoch t0 to t (t - t0). */
    public double secondsSinceEpoch(Instant t) {
        Objects.requireNonNull(t, "t must not be null");
        long ds = t.getEpochSecond() - epoch.getEpochSecond();
        int dns = t.getNano() - epoch.getNano();
        return (double) ds + dns * 1e-9;
    }

    /**
     * Mean anomaly at time t [rad], normalized to [0, 2π).
     * M(t) = M0 + n (t - t0)
     */
    public double meanAnomalyAt(Instant t) {
        double M = meanAnomalyAtEpochRad + meanMotionRadPerSec() * secondsSinceEpoch(t);
        return wrapToTwoPi(M);
    }

    /**
     * Eccentric anomaly E from mean anomaly M and eccentricity e (elliptic),
     * solved by Newton-Raphson with a robust starter. Returns E ∈ [0, 2π).
     */
    public static double eccentricAnomalyFromMean(double M, double e) {
        if (e < AreoBodyConstants.EPS) {
            return wrapToTwoPi(M); // circular limit
        }
        M = wrapToTwoPi(M);
        // Initial guess (good across e):
        // Danby starter approximation
        double sinM = Math.sin(M);
        double cosM = Math.cos(M);
        double E = M + (e * sinM) / (1.0 - sin(M + e) + sinM);

        // Newton iterations
        for (int k = 0; k < 50; k++) {
            double f = E - e * Math.sin(E) - M;
            double fp = 1.0 - e * Math.cos(E);
            double dE = -f / fp;
            E += dE;
            if (Math.abs(dE) < 1e-12) break;
        }
        return wrapToTwoPi(E);
    }

    /**
     * True anomaly ν from mean anomaly M and eccentricity e (elliptic).
     * Returns ν ∈ [0, 2π).
     */
    public static double trueAnomalyFromMean(double M, double e) {
        double E = eccentricAnomalyFromMean(M, e);
        return trueAnomalyFromEccentric(E, e);
    }

    /** True anomaly ν from eccentric anomaly E and eccentricity e, ν ∈ [0, 2π). */
    public static double trueAnomalyFromEccentric(double E, double e) {
        double cosE = Math.cos(E);
        double sinE = Math.sin(E);
        double denom = 1.0 - e * cosE;
        double sqrt1me2 = Math.sqrt(Math.max(0.0, 1.0 - e * e));
        double cosNu = (cosE - e) / denom;
        double sinNu = (sqrt1me2 * sinE) / denom;
        double nu = Math.atan2(sinNu, cosNu);
        return wrapToTwoPi(nu);
    }

    /**
     * Radius r [m] at true anomaly ν for this orbit: r = p / (1 + e cos ν).
     */
    public double radiusAtTrueAnomaly(double trueAnomalyRad) {
        return semiLatusRectumM() / (1.0 + eccentricity * Math.cos(trueAnomalyRad));
    }

    /**
     * Returns a new element set whose epoch is shifted to {@code newEpoch} while
     * preserving the absolute orbital phase (i.e., M(newEpoch) stays the same,
     * and becomes the new M0).
     */
    public KeplerianElements shiftEpoch(Instant newEpoch) {
        double M_at_new = meanAnomalyAt(newEpoch);
        return ofMetersRadians(semiMajorAxisM, eccentricity, inclinationRad, raanRad, argOfPeriapsisRad, M_at_new, newEpoch);
    }

    /** Returns a new element set with M0 normalized to [0, 2π). */
    public KeplerianElements normalized() {
        return ofMetersRadians(semiMajorAxisM, eccentricity, inclinationRad, raanRad, argOfPeriapsisRad, wrapToTwoPi(meanAnomalyAtEpochRad), epoch);
    }

    // ---------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------

    /** Wrap angle to [0, 2π). */
    public static double wrapToTwoPi(double angleRad) {
        double x = angleRad % AreoBodyConstants.TWO_PI;
        if (x < 0) x += AreoBodyConstants.TWO_PI;
        return x;
    }

    // ---------------------------------------------------------------------
    // Equality & debug
    // ---------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeplerianElements)) return false;
        KeplerianElements that = (KeplerianElements) o;
        return Double.doubleToLongBits(semiMajorAxisM) == Double.doubleToLongBits(that.semiMajorAxisM)
                && Double.doubleToLongBits(eccentricity) == Double.doubleToLongBits(that.eccentricity)
                && Double.doubleToLongBits(inclinationRad) == Double.doubleToLongBits(that.inclinationRad)
                && Double.doubleToLongBits(raanRad) == Double.doubleToLongBits(that.raanRad)
                && Double.doubleToLongBits(argOfPeriapsisRad) == Double.doubleToLongBits(that.argOfPeriapsisRad)
                && Double.doubleToLongBits(meanAnomalyAtEpochRad) == Double.doubleToLongBits(that.meanAnomalyAtEpochRad)
                && epoch.equals(that.epoch);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(semiMajorAxisM);
        result = 31 * result + Double.hashCode(eccentricity);
        result = 31 * result + Double.hashCode(inclinationRad);
        result = 31 * result + Double.hashCode(raanRad);
        result = 31 * result + Double.hashCode(argOfPeriapsisRad);
        result = 31 * result + Double.hashCode(meanAnomalyAtEpochRad);
        result = 31 * result + epoch.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "Kep[a=%.3f km, e=%.6f, i=%.4f deg, Ω=%.4f deg, ω=%.4f deg, M0=%.4f deg, epoch=%s]",
                semiMajorAxisM / 1_000.0,
                eccentricity,
                Math.toDegrees(inclinationRad),
                Math.toDegrees(raanRad),
                Math.toDegrees(argOfPeriapsisRad),
                Math.toDegrees(meanAnomalyAtEpochRad),
                epoch);
    }
}
