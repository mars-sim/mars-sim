/*
 * This file is part of the Mars Simulation Project (mars-sim).
 * License: GPL-3.0 (see the project root LICENSE file).
 *
 * Purpose:
 *   Immutable representation of a surface (or near-surface) location on Mars,
 *   expressed as planetocentric (geocentric) latitude, east-positive longitude,
 *   and altitude above the Mars mean radius. Includes utilities for common
 *   geometry used by the orbital/comms layer (surface distance, central angle,
 *   conversion to/from Mars-fixed (MCMF) vectors, simple visibility/elevation
 *   tests against a satellite position, and one-way light time).
 *
 * Conventions:
 *   - Latitude is planetocentric (angle from equatorial plane to the radius
 *     vector) in radians. Longitude is east-positive in (-π, π].
 *   - Altitude is above the Mars mean (spherical) radius; negative altitudes
 *     are permitted (e.g., Hellas basin), but R + h must remain > 0.
 *   - All computations assume a spherical Mars with radius
 *     AreoBodyConstants.MARS_MEAN_RADIUS_M unless stated otherwise.
 */

package com.mars_sim.core.orbit;

import java.util.Locale;
import java.util.Objects;

public final class GroundPoint {

    // ---------------------------------------------------------------------
    // Fields (immutable)
    // ---------------------------------------------------------------------

    /** Planetocentric latitude [rad]. */
    private final double latitudeRad;

    /** East-positive longitude [rad], normalized to (-π, π]. */
    private final double longitudeRad;

    /** Altitude above mean radius [m]. */
    private final double altitudeM;

    // ---------------------------------------------------------------------
    // Construction
    // ---------------------------------------------------------------------

    /** Construct from radians (lat, lon) and altitude [m]. */
    public GroundPoint(double latitudeRad, double longitudeRad, double altitudeM) {
        if (!Double.isFinite(latitudeRad) || !Double.isFinite(longitudeRad) || !Double.isFinite(altitudeM)) {
            throw new IllegalArgumentException("lat/lon/alt must be finite");
        }
        // Keep latitude in [-π, π]; reflect to [-π/2, +π/2] if outside.
        double lat = normalizeLatitude(latitudeRad);
        double lon = normalizeLongitude(longitudeRad);

        // Reflect poles if lat was outside principal range
        if (lat > Math.PI / 2.0) {
            lat = Math.PI - lat;
            lon = normalizeLongitude(lon + Math.PI);
        } else if (lat < -Math.PI / 2.0) {
            lat = -Math.PI - lat;
            lon = normalizeLongitude(lon + Math.PI);
        }

        // Enforce non-degenerate radius (R + h > 0)
        double minAlt = -AreoBodyConstants.MARS_MEAN_RADIUS_M + 1e-3; // leave a tiny positive radius
        if (altitudeM < minAlt) {
            throw new IllegalArgumentException("Altitude too negative; results in non-physical radius.");
        }

        this.latitudeRad = lat;
        this.longitudeRad = lon;
        this.altitudeM = altitudeM;
    }

    /** Factory: inputs in degrees for angles, meters for altitude. */
    public static GroundPoint ofDegrees(double latitudeDeg, double longitudeDeg, double altitudeM) {
        return new GroundPoint(Math.toRadians(latitudeDeg), Math.toRadians(longitudeDeg), altitudeM);
    }

    /** Factory: from an MCMF (Mars-fixed) position vector [m]. */
    public static GroundPoint fromFixedVector(double[] rFixed) {
        Objects.requireNonNull(rFixed, "rFixed");
        if (rFixed.length != 3) throw new IllegalArgumentException("rFixed must be length 3");
        double x = rFixed[0], y = rFixed[1], z = rFixed[2];
        double rho = Math.hypot(x, y);
        double rmag = Math.hypot(rho, z);
        double lat = Math.atan2(z, rho);          // planetocentric latitude
        double lon = Math.atan2(y, x);            // east-positive
        double alt = rmag - AreoBodyConstants.MARS_MEAN_RADIUS_M;
        return new GroundPoint(lat, lon, alt);
    }

    // ---------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------

    public double latitudeRad()   { return latitudeRad; }
    public double longitudeRad()  { return longitudeRad; }
    public double altitudeM()     { return altitudeM; }

    public double latitudeDeg()   { return Math.toDegrees(latitudeRad); }
    public double longitudeDeg()  { return Math.toDegrees(longitudeRad); }

    /** Planet radius at this point (spherical model) = R_mean + altitude. */
    public double radiusM() { return AreoBodyConstants.MARS_MEAN_RADIUS_M + altitudeM; }

    // ---------------------------------------------------------------------
    // Conversions
    // ---------------------------------------------------------------------

    /**
     * Convert to a Mars-fixed (MCMF) position vector [m], spherical planet model.
     * r = (R + h) [cosφ cosλ, cosφ sinλ, sinφ]
     */
    public double[] toFixedVector() {
        double R = radiusM();
        double clat = Math.cos(latitudeRad);
        double x = R * clat * Math.cos(longitudeRad);
        double y = R * clat * Math.sin(longitudeRad);
        double z = R * Math.sin(latitudeRad);
        return new double[] { x, y, z };
    }

    /** Local up/zenith unit vector in MCMF (spherical model). */
    public double[] upUnit() {
        double[] r = toFixedVector();
        double R = Math.sqrt(r[0]*r[0] + r[1]*r[1] + r[2]*r[2]);
        return new double[] { r[0]/R, r[1]/R, r[2]/R };
    }

    /** Local east unit vector in MCMF (points toward increasing longitude). */
    public double[] eastUnit() {
        double lon = longitudeRad;
        return new double[] { -Math.sin(lon), Math.cos(lon), 0.0 };
    }

    /** Local north unit vector in MCMF (tangent, toward increasing latitude). */
    public double[] northUnit() {
        double lat = latitudeRad, lon = longitudeRad;
        double slat = Math.sin(lat), clat = Math.cos(lat);
        double clon = Math.cos(lon), slon = Math.sin(lon);
        return new double[] { -slat * clon, -slat * slon, clat };
    }

    // ---------------------------------------------------------------------
    // Surface geometry (spherical)
    // ---------------------------------------------------------------------

    /**
     * Central angle α between this point and another [rad], spherical planet.
     * Uses the haversine formulation for numerical robustness.
     */
    public double centralAngleRad(GroundPoint other) {
        Objects.requireNonNull(other, "other");
        double dLat = other.latitudeRad - this.latitudeRad;
        double dLon = normalizeLongitude(other.longitudeRad - this.longitudeRad);
        double s1 = Math.sin(dLat * 0.5);
        double s2 = Math.sin(dLon * 0.5);
        double a = s1*s1 + Math.cos(latitudeRad) * Math.cos(other.latitudeRad) * s2*s2;
        a = Math.min(1.0, Math.max(0.0, a));
        return 2.0 * Math.asin(Math.sqrt(a));
    }

    /**
     * Great-circle surface distance [m] between this point and another.
     * Uses mean Mars radius plus the average altitude of the two points.
     */
    public double surfaceDistanceM(GroundPoint other) {
        double alpha = centralAngleRad(other);
        double effectiveR = AreoBodyConstants.MARS_MEAN_RADIUS_M + 0.5 * (this.altitudeM + other.altitudeM);
        return effectiveR * alpha;
    }

    // ---------------------------------------------------------------------
    // Satellite visibility / link helpers (spherical Mars)
    // ---------------------------------------------------------------------

    /**
     * Elevation angle of a satellite at MCMF position {@code satFixed} as seen
     * from this ground point [rad]. Positive means above local horizon; 0 at
       horizon; negative means below.
     *
     * Computation:
     *   e = asin( ( (sat - r_g) · û ) / |sat - r_g| ), where û is local zenith.
     */
    public double elevationRad(double[] satFixed) {
        Objects.requireNonNull(satFixed, "satFixed");
        if (satFixed.length != 3) throw new IllegalArgumentException("satFixed must be length 3");
        double[] rg = toFixedVector();
        double vx = satFixed[0] - rg[0];
        double vy = satFixed[1] - rg[1];
        double vz = satFixed[2] - rg[2];
        double vmag = Math.sqrt(vx*vx + vy*vy + vz*vz);
        if (!(vmag > 0.0)) return -Math.PI / 2.0; // coincident/degenerate → treat as below horizon
        double[] up = new double[] { rg[0] / radiusM(), rg[1] / radiusM(), rg[2] / radiusM() };
        double dot = vx*up[0] + vy*up[1] + vz*up[2];
        double arg = dot / vmag;
        // Clamp for safety
        arg = Math.max(-1.0, Math.min(1.0, arg));
        return Math.asin(arg);
    }

    /** True if the satellite at {@code satFixed} is above the local horizon. */
    public boolean isVisibleFrom(double[] satFixed) {
        return elevationRad(satFixed) > 0.0;
    }

    /** Straight-line range [m] to satellite at {@code satFixed}. */
    public double rangeToM(double[] satFixed) {
        Objects.requireNonNull(satFixed, "satFixed");
        if (satFixed.length != 3) throw new IllegalArgumentException("satFixed must be length 3");
        double[] rg = toFixedVector();
        double dx = satFixed[0] - rg[0];
        double dy = satFixed[1] - rg[1];
        double dz = satFixed[2] - rg[2];
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /** One-way light time [s] to satellite at {@code satFixed}. */
    public double oneWayLightTimeSeconds(double[] satFixed) {
        return rangeToM(satFixed) / AreoBodyConstants.SPEED_OF_LIGHT_M_S;
    }

    // ---------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------

    /** Normalize longitude to (-π, π]. */
    public static double normalizeLongitude(double lonRad) {
        double x = lonRad % (2.0 * Math.PI);
        if (x <= -Math.PI) x += 2.0 * Math.PI;
        if (x > Math.PI)   x -= 2.0 * Math.PI;
        return x;
    }

    /** Normalize latitude to [-π, π]. (Helper for robust pole handling.) */
    private static double normalizeLatitude(double latRad) {
        double x = latRad % (2.0 * Math.PI);
        if (x < -Math.PI) x += 2.0 * Math.PI;
        if (x >  Math.PI) x -= 2.0 * Math.PI;
        return x;
    }

    // ---------------------------------------------------------------------
    // Equality & debug
    // ---------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroundPoint)) return false;
        GroundPoint that = (GroundPoint) o;
        return Double.doubleToLongBits(latitudeRad)  == Double.doubleToLongBits(that.latitudeRad)
            && Double.doubleToLongBits(longitudeRad) == Double.doubleToLongBits(that.longitudeRad)
            && Double.doubleToLongBits(altitudeM)    == Double.doubleToLongBits(that.altitudeM);
    }

    @Override
    public int hashCode() {
        int h = Double.hashCode(latitudeRad);
        h = 31 * h + Double.hashCode(longitudeRad);
        h = 31 * h + Double.hashCode(altitudeM);
        return h;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
            "GroundPoint[lat=%.6f° lon=%.6f° alt=%.1f m]",
            latitudeDeg(), longitudeDeg(), altitudeM);
    }
}
