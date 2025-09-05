/*
 * This file is part of the Mars Simulation Project (mars-sim).
 * License: GPL-3.0 (see the project root LICENSE file).
 *
 * Purpose:
 *   Stateless helpers for orbital visibility and coverage geometry:
 *     - Ground ↔ satellite line-of-sight (LOS) tests (spherical Mars).
 *     - Coverage / footprint half-angles (with optional min elevation).
 *     - Central-angle / great-circle distance helpers.
 *     - Free-space path loss and light-time utilities.
 *     - Footprint circle polygon generation (for drawing on maps).
 *
 * Conventions:
 *   - "Fixed" coordinates are Mars-centered, Mars-fixed (MCMF).
 *   - GroundPoint uses planetocentric lat/lon (east-positive), altitude
 *     above the Mars mean radius (spherical model).
 *   - Unless stated, Mars radius refers to AreoBodyConstants.MARS_MEAN_RADIUS_M.
 *
 * Notes:
 *   - LOS tests here ignore terrain occlusion; add terrain masking later.
 *   - The "minimum elevation" coverage formula below is exact for a spherical
 *     planet with different altitudes at the two endpoints; it reduces to the
 *     standard horizon half-angle when e_min = 0.
 */

package com.mars_sim.core.orbit;

import java.util.Locale;
import java.util.Objects;

public final class Visibility {

    private Visibility() {
        throw new AssertionError("No instances");
    }

    // ---------------------------------------------------------------------
    // Small result container for LOS queries
    // ---------------------------------------------------------------------

    public static final class LosResult {
        /** Ground site used for the query. */
        public final GroundPoint ground;
        /** Subsatellite point (lat/lon taken from satellite vector; altitude = sat altitude). */
        public final GroundPoint subsatellite;
        /** Satellite altitude above mean radius [m]. */
        public final double satelliteAltitudeM;
        /** Geocentric central angle between ground site and subsatellite point [rad]. */
        public final double centralAngleRad;
        /** Elevation angle of satellite seen from ground [rad]. */
        public final double elevationRad;
        /** Slant range [m]. */
        public final double rangeM;
        /** One-way light time [s]. */
        public final double owltSeconds;
        /** True if elevation >= minElevationRad passed to evaluate(). */
        public final boolean visible;

        private LosResult(GroundPoint ground,
                          GroundPoint subsatellite,
                          double satelliteAltitudeM,
                          double centralAngleRad,
                          double elevationRad,
                          double rangeM,
                          double owltSeconds,
                          boolean visible) {
            this.ground = ground;
            this.subsatellite = subsatellite;
            this.satelliteAltitudeM = satelliteAltitudeM;
            this.centralAngleRad = centralAngleRad;
            this.elevationRad = elevationRad;
            this.rangeM = rangeM;
            this.owltSeconds = owltSeconds;
            this.visible = visible;
        }

        public double elevationDeg() { return Math.toDegrees(elevationRad); }
        public double centralAngleDeg() { return Math.toDegrees(centralAngleRad); }

        @Override
        public String toString() {
            return String.format(Locale.ROOT,
                    "LOS[vis=%s, elev=%.3f deg, α=%.3f deg, range=%.0f m, OWLT=%.3f s, sub=(%.4f°, %.4f°), h_sat=%.0f m]",
                    visible ? "YES" : "NO",
                    elevationDeg(),
                    centralAngleDeg(),
                    rangeM,
                    owltSeconds,
                    subsatellite.latitudeDeg(),
                    subsatellite.longitudeDeg(),
                    satelliteAltitudeM);
        }
    }

    // ---------------------------------------------------------------------
    // Ground ↔ Satellite LOS (vector form, exact on a sphere)
    // ---------------------------------------------------------------------

    /**
     * Evaluate geometry between a ground point and a satellite at Mars-fixed position.
     * Uses exact vector geometry on a spherical planet (ignores terrain).
     *
     * @param ground           Ground site (lat/lon/alt)
     * @param satFixed         Satellite position in MCMF [m], length-3
     * @param minElevationRad  Minimum required elevation [rad] (e.g., 0 for horizon,
     *                         or Math.toRadians(5) for a 5° mask)
     */
    public static LosResult evaluateGroundToSatellite(GroundPoint ground,
                                                      double[] satFixed,
                                                      double minElevationRad) {
        Objects.requireNonNull(ground, "ground");
        Objects.requireNonNull(satFixed, "satFixed");
        if (satFixed.length != 3) throw new IllegalArgumentException("satFixed must be length 3");

        // Subsatellite geocentric lat/lon; altitude is satellite altitude.
        GroundPoint subsat = GroundPoint.fromFixedVector(satFixed);

        // Geometry
        double elev = ground.elevationRad(satFixed);
        double range = ground.rangeToM(satFixed);
        double owlt = range / AreoBodyConstants.SPEED_OF_LIGHT_M_S;
        double alpha = ground.centralAngleRad(new GroundPoint(subsat.latitudeRad(), subsat.longitudeRad(), 0.0));

        // Satellite altitude above mean radius:
        double rmag = Math.sqrt(satFixed[0] * satFixed[0] + satFixed[1] * satFixed[1] + satFixed[2] * satFixed[2]);
        double hSat = rmag - AreoBodyConstants.MARS_MEAN_RADIUS_M;

        boolean visible = elev >= minElevationRad;

        return new LosResult(ground, subsat, hSat, alpha, elev, range, owlt, visible);
    }

    /** Convenience overload: horizon mask (min elevation = 0). */
    public static LosResult evaluateGroundToSatellite(GroundPoint ground, double[] satFixed) {
        return evaluateGroundToSatellite(ground, satFixed, 0.0);
    }

    // ---------------------------------------------------------------------
    // Coverage / footprint half-angle math (spherical)
    // ---------------------------------------------------------------------

    /**
     * Horizon half-angle ψ for an observer at altitude h above a sphere of radius R.
     * ψ(h) = arccos( R / (R + h) )
     */
    public static double horizonHalfAngleRad(double planetRadiusM, double altitudeM) {
        double R = planetRadiusM;
        double r = R + Math.max(0.0, altitudeM);
        double cosPsi = clamp(R / r, -1.0, 1.0);
        return Math.acos(cosPsi);
    }

    /** Horizon half-angle ψ for Mars using mean radius. */
    public static double horizonHalfAngleRad(double altitudeM) {
        return horizonHalfAngleRad(AreoBodyConstants.MARS_MEAN_RADIUS_M, altitudeM);
    }

    /**
     * Maximum central angle α_max for mutual visibility between two points at
     * altitudes h1 and h2 (above the same sphere), using horizon geometry:
     * α_max ≈ ψ(h1) + ψ(h2).
     * For ground↔sat this gives a quick approximation (exact LOS is via vectors).
     */
    public static double mutualVisibilityMaxCentralAngleRad(double planetRadiusM, double h1, double h2) {
        return horizonHalfAngleRad(planetRadiusM, h1) + horizonHalfAngleRad(planetRadiusM, h2);
    }

    /** Convenience for Mars. */
    public static double mutualVisibilityMaxCentralAngleRad(double h1, double h2) {
        return mutualVisibilityMaxCentralAngleRad(AreoBodyConstants.MARS_MEAN_RADIUS_M, h1, h2);
    }

    /**
     * Exact maximum central angle as a function of minimum elevation e_min.
     * For a ground site at radius Rg = R + h_g and a satellite at Rs = R + h_s:
     *
     *   Let ρ = Rg / Rs and e = e_min.
     *   Then  cos(α_max) = ρ cos^2 e + sin e * sqrt(1 - ρ^2 cos^2 e).
     *
     * Special cases:
     *   - e = 0 → α_max = arccos(Rg / Rs) (horizon).
     *   - e = 90° → α_max = 0 (satellite must be at zenith).
     */
    public static double maxCentralAngleForMinElevationRad(double planetRadiusM,
                                                           double groundAltitudeM,
                                                           double satelliteAltitudeM,
                                                           double minElevationRad) {
        double Rg = planetRadiusM + Math.max(groundAltitudeM, -planetRadiusM + 1e-3);
        double Rs = planetRadiusM + Math.max(0.0, satelliteAltitudeM);
        double e = clamp(minElevationRad, 0.0, Math.PI / 2.0);

        double cosE = Math.cos(e);
        double sinE = Math.sin(e);
        double rho = Rg / Rs;

        double under = 1.0 - (rho * rho) * (cosE * cosE);
        if (under < 0.0) under = 0.0; // guard tiny negatives

        double cosAlpha = rho * (cosE * cosE) + sinE * Math.sqrt(under);
        cosAlpha = clamp(cosAlpha, -1.0, 1.0);

        return Math.acos(cosAlpha);
    }

    /** Convenience for Mars. */
    public static double maxCentralAngleForMinElevationRad(double groundAltitudeM,
                                                           double satelliteAltitudeM,
                                                           double minElevationRad) {
        return maxCentralAngleForMinElevationRad(
                AreoBodyConstants.MARS_MEAN_RADIUS_M, groundAltitudeM, satelliteAltitudeM, minElevationRad);
    }

    /**
     * Check footprint membership using the min-elevation constraint (exact spherical formula).
     * Uses only lat/lon for central angle; altitudes enter via the α_max(e_min) formula.
     */
    public static boolean isWithinFootprint(GroundPoint ground,
                                            GroundPoint subsatellite,
                                            double satelliteAltitudeM,
                                            double minElevationRad) {
        Objects.requireNonNull(ground, "ground");
        Objects.requireNonNull(subsatellite, "subsatellite");

        double alpha = ground.centralAngleRad(new GroundPoint(subsatellite.latitudeRad(), subsatellite.longitudeRad(), 0.0));
        double alphaMax = maxCentralAngleForMinElevationRad(ground.altitudeM(), satelliteAltitudeM, minElevationRad);
        return alpha <= alphaMax + 1e-12; // small tolerance
    }

    /** Check footprint membership with horizon mask (min elevation = 0). */
    public static boolean isWithinFootprint(GroundPoint ground, GroundPoint subsatellite, double satelliteAltitudeM) {
        return isWithinFootprint(ground, subsatellite, satelliteAltitudeM, 0.0);
    }

    // ---------------------------------------------------------------------
    // Surface geometry helpers
    // ---------------------------------------------------------------------

    /** Central angle between two ground points [rad]. */
    public static double centralAngleRad(GroundPoint a, GroundPoint b) {
        return a.centralAngleRad(b);
    }

    /** Great-circle distance between two ground points along the surface [m]. */
    public static double surfaceDistanceM(GroundPoint a, GroundPoint b) {
        return a.surfaceDistanceM(b);
    }

    // ---------------------------------------------------------------------
    // Free-space path loss & light time
    // ---------------------------------------------------------------------

    /**
     * Free-space path loss (FSPL) in dB with distance in meters and frequency in Hz:
     *   FSPL(dB) = 20 log10(d) + 20 log10(f) + 147.55
     */
    public static double fsplDbMetersHz(double rangeMeters, double frequencyHz) {
        if (!(rangeMeters > 0.0) || !(frequencyHz > 0.0)) return Double.POSITIVE_INFINITY;
        return 20.0 * log10(rangeMeters) + 20.0 * log10(frequencyHz) + 147.55;
    }

    /** One-way light time [s] from range [m]. */
    public static double owltSeconds(double rangeMeters) {
        return rangeMeters / AreoBodyConstants.SPEED_OF_LIGHT_M_S;
    }

    // ---------------------------------------------------------------------
    // Footprint circle polygon generation (for map overlays)
    // ---------------------------------------------------------------------

    /**
     * Generate a geodesic circle (constant central angle) on the spherical surface.
     *
     * @param centerOnSurface  Circle center on the surface (use subsatellite lat/lon with alt=0)
     * @param halfAngleRad     Circle half-angle (e.g., horizon ψ or α_max(e_min))
     * @param samples          Number of vertices (minimum 4; typical 64–256 for smoothness)
     */
    public static GroundPoint[] circleOnSphere(GroundPoint centerOnSurface, double halfAngleRad, int samples) {
        Objects.requireNonNull(centerOnSurface, "centerOnSurface");
        if (samples < 4) throw new IllegalArgumentException("samples must be >= 4");
        double lat0 = centerOnSurface.latitudeRad();
        double lon0 = centerOnSurface.longitudeRad();
        double sinLat0 = Math.sin(lat0);
        double cosLat0 = Math.cos(lat0);
        double sinPsi = Math.sin(halfAngleRad);
        double cosPsi = Math.cos(halfAngleRad);

        GroundPoint[] pts = new GroundPoint[samples];
        double dTheta = 2.0 * Math.PI / samples;
        for (int k = 0; k < samples; k++) {
            double theta = k * dTheta;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            // Spherical direct geodesic (from center bearing theta, range ψ)
            double sinLat = sinLat0 * cosPsi + cosLat0 * sinPsi * cosTheta;
            double lat = Math.asin(clamp(sinLat, -1.0, 1.0));

            double y = sinTheta * sinPsi * cosLat0;
            double x = cosPsi - sinLat0 * Math.sin(lat);
            double dLon = Math.atan2(y, x);

            double lon = GroundPoint.normalizeLongitude(lon0 + dLon);

            // Points lie on the surface (alt = 0 relative to mean radius)
            pts[k] = new GroundPoint(lat, lon, 0.0);
        }
        return pts;
    }

    /**
     * Convenience: footprint circle for a satellite given its subsatellite lat/lon and altitude.
     * If you want a min elevation mask, compute the half-angle via maxCentralAngleForMinElevationRad.
     *
     * @param subsatelliteLLA  Subsatellite point (lat/lon used; altitude ignored here)
     * @param satelliteAltitudeM  Satellite altitude above mean radius [m]
     * @param samples          Number of vertices
     */
    public static GroundPoint[] footprintCircle(GroundPoint subsatelliteLLA,
                                                double satelliteAltitudeM,
                                                int samples) {
        double psi = horizonHalfAngleRad(satelliteAltitudeM);
        GroundPoint centerSurface = new GroundPoint(subsatelliteLLA.latitudeRad(), subsatelliteLLA.longitudeRad(), 0.0);
        return circleOnSphere(centerSurface, psi, samples);
    }

    // ---------------------------------------------------------------------
    // Internal numeric helpers
    // ---------------------------------------------------------------------

    private static double log10(double x) {
        return Math.log(x) / Math.log(10.0);
    }

    private static double clamp(double x, double lo, double hi) {
        return (x < lo) ? lo : (x > hi) ? hi : x;
    }
}
