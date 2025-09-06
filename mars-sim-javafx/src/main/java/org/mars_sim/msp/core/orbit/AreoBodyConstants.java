/*
 * This file is part of the Mars Simulation Project (mars-sim).
 * License: GPL-3.0 (see the project root LICENSE file).
 *
 * Purpose:
 *   Canonical areocentric (Mars) physical constants and a few derived
 *   areostationary (areosynchronous) orbit parameters for use by the
 *   orbital / comms layer.
 *
 * Notes:
 *   - All units are SI unless otherwise indicated.
 *   - Values follow common references (e.g., IAU/JPL) and are suitable
 *     for two-body propagation and first-order geometry/coverage math.
 *   - Areostationary quantities are derived at class load from the
 *     fundamental constants below; comments include their nominal values.
 */

package org.mars_sim.msp.core.orbit;

import java.time.Instant;
import java.util.Locale;

public final class AreoBodyConstants {

    private AreoBodyConstants() {
        throw new AssertionError("No instances");
    }

    // ---------------------------------------------------------------------
    // Universal / numeric helpers
    // ---------------------------------------------------------------------

    /** Speed of light in vacuum [m/s]. */
    public static final double SPEED_OF_LIGHT_M_S = 299_792_458.0;

    /** 2π convenience constant. */
    public static final double TWO_PI = 2.0 * Math.PI;

    /** Degrees↔radians conversion factors. */
    public static final double DEG2RAD = Math.PI / 180.0;
    public static final double RAD2DEG = 180.0 / Math.PI;

    /** Generic numeric tolerance for orbital math comparisons. */
    public static final double EPS = 1e-12;

    // ---------------------------------------------------------------------
    // Mars (areocentric) geodetic & gravity constants
    // ---------------------------------------------------------------------

    /** Mars standard gravitational parameter μ = GM [m^3/s^2]. */
    public static final double MARS_GM_M3_S2 = 42_828_370_000_000.0; // 4.282837e13

    /** Mars equatorial radius (IAU) [m]. */
    public static final double MARS_EQUATORIAL_RADIUS_M = 3_396_190.0; // 3396.19 km

    /** Mars polar radius (IAU) [m]. */
    public static final double MARS_POLAR_RADIUS_M = 3_376_200.0; // 3376.20 km

    /** Mars mean (spherical) radius [m]. */
    public static final double MARS_MEAN_RADIUS_M = 3_389_500.0; // 3389.5 km

    /** Mars dynamic form factor J2 [-] (for future perturbations, optional). */
    public static final double MARS_J2 = 1.96045e-3;

    /** Geometric flattening f = (a - b) / a [-]. */
    public static final double MARS_FLATTENING =
            (MARS_EQUATORIAL_RADIUS_M - MARS_POLAR_RADIUS_M) / MARS_EQUATORIAL_RADIUS_M;

    // ---------------------------------------------------------------------
    // Mars rotation / timekeeping
    // ---------------------------------------------------------------------

    /** Mars sidereal rotation period (relative to stars) [s]. */
    public static final double MARS_SIDEREAL_DAY_S = 88_642.663; // 24h 37m 22.663s

    /** Mars solar day ("sol", noon-to-noon) [s]. */
    public static final double MARS_SOLAR_DAY_S = 88_775.244; // 24h 39m 35.244s

    /** Mars rotation rate ω (areocentric inertial → areo-fixed) [rad/s]. */
    public static final double MARS_ROTATION_RATE_RAD_S = TWO_PI / MARS_SIDEREAL_DAY_S; // ≈ 7.088218e-05

    /** Default epoch for orbital elements/configs (can be overridden by XML). */
    public static final Instant DEFAULT_EPOCH = Instant.parse("2025-01-01T00:00:00Z");

    // ---------------------------------------------------------------------
    // Areostationary (areosynchronous) derived parameters
    //   Defined by T_sidereal = orbital period; assumes circular equatorial orbit.
    // ---------------------------------------------------------------------

    /** Areostationary semi-major axis a_areo [m]; derived from μ and ω. */
    public static final double AREOSTATIONARY_SEMIMAJOR_AXIS_M;

    /** Areostationary altitude above mean radius h_areo [m] (a - R_mean). */
    public static final double AREOSTATIONARY_ALTITUDE_M;

    /**
     * Ground coverage half-angle ψ for areostationary altitude [rad].
     * Spherical geometry: ψ = arccos(R / (R + h)).
     */
    public static final double AREOSTATIONARY_COVERAGE_HALF_ANGLE_RAD;

    /** Same as above, in degrees. */
    public static final double AREOSTATIONARY_COVERAGE_HALF_ANGLE_DEG;

    /**
     * One-way light time from areostat straight down to sub-satellite point [s].
     * Approximated as h / c (good for planning, not a full link budget).
     */
    public static final double AREOSTATIONARY_BORE_SIGHT_OWLT_S;

    /**
     * Empirical “parking” longitudes (degrees East) often cited as relatively
     * stable areostationary equilibria under Mars' gravity field.
     * Convention: degrees East in [-180, +180]; negative = West.
     * Usage is optional and informational.
     */
    public static final double[] AREO_STABLE_LONGITUDES_DEG_EAST = new double[] { -17.92, 167.83 };

    static {
        // a = cbrt( μ / ω^2 )
        AREOSTATIONARY_SEMIMAJOR_AXIS_M =
                Math.cbrt(MARS_GM_M3_S2 / (MARS_ROTATION_RATE_RAD_S * MARS_ROTATION_RATE_RAD_S)); // ≈ 20_427_684 m

        AREOSTATIONARY_ALTITUDE_M = AREOSTATIONARY_SEMIMAJOR_AXIS_M - MARS_MEAN_RADIUS_M; // ≈ 17_038_184 m

        AREOSTATIONARY_COVERAGE_HALF_ANGLE_RAD =
                Math.acos(MARS_MEAN_RADIUS_M / (MARS_MEAN_RADIUS_M + AREOSTATIONARY_ALTITUDE_M)); // ≈ 1.40410 rad

        AREOSTATIONARY_COVERAGE_HALF_ANGLE_DEG =
                AREOSTATIONARY_COVERAGE_HALF_ANGLE_RAD * RAD2DEG; // ≈ 80.449°

        AREOSTATIONARY_BORE_SIGHT_OWLT_S = AREOSTATIONARY_ALTITUDE_M / SPEED_OF_LIGHT_M_S; // ≈ 0.05683 s
    }

    // ---------------------------------------------------------------------
    // Small utility helpers that are handy in tests and debug UIs
    // ---------------------------------------------------------------------

    /**
     * Computes the spherical-planet coverage half-angle ψ [rad] for a satellite
     * at a given altitude above Mars mean radius.
     */
    public static double coverageHalfAngleRad(double altitudeMeters) {
        double h = Math.max(0.0, altitudeMeters);
        return Math.acos(MARS_MEAN_RADIUS_M / (MARS_MEAN_RADIUS_M + h));
    }

    /** Returns a concise one-line summary of key areostationary figures. */
    public static String summary() {
        return String.format(Locale.ROOT,
            "μ=%.6e m^3/s^2, R_mean=%.0f km, ω=%.9f rad/s, a_areo=%.0f km, h_areo=%.0f km, ψ=%.2f°, OWLT=%.3f s",
            MARS_GM_M3_S2,
            MARS_MEAN_RADIUS_M / 1_000.0,
            MARS_ROTATION_RATE_RAD_S,
            AREOSTATIONARY_SEMIMAJOR_AXIS_M / 1_000.0,
            AREOSTATIONARY_ALTITUDE_M / 1_000.0,
            AREOSTATIONARY_COVERAGE_HALF_ANGLE_DEG,
            AREOSTATIONARY_BORE_SIGHT_OWLT_S
        );
    }
}
