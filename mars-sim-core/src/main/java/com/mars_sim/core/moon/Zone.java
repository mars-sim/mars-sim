package com.mars_sim.core.moon;

import com.mars_sim.core.time.ClockPulse;

import java.io.Serializable;
import java.util.Objects;

/**
 * A rectangular selenographic (lunar) surface zone defined by latitude/longitude bounds.
 *
 * <p><b>Units & conventions</b>
 * <ul>
 *   <li>Latitude in degrees, range [-90, +90]. North positive.</li>
 *   <li>Longitude in degrees, normalized and stored internally in the range [-180, +180).</li>
 *   <li>Angles are validated on write; NaN/Inf is rejected.</li>
 *   <li>Dateline-crossing (west &gt; east after normalization) is supported.</li>
 * </ul>
 *
 * <p><b>Design goals</b>
 * <ul>
 *   <li>Serializable, lightweight POJO; no engine/runtime coupling.</li>
 *   <li>Immutable-style helpers (builder, copy/with) without breaking classic setters.</li>
 *   <li>Helpers for robust geographic logic (contains, intersects, expansion).</li>
 *   <li>Support for 0–360° and −180–+180° longitude conventions.</li>
 *   <li>Preserve interop with existing code that uses legacy APIs like
 *       {@code Zone(ZoneType, Colony, boolean)}, {@code timePassing(ClockPulse)},
 *       {@code getArea()}, {@code getGrowthPercent()}, and {@code getZoneType()}.</li>
 * </ul>
 */
public class Zone implements Serializable {

    /** Serialization ID for compatibility. */
    private static final long serialVersionUID = 1L;

    /** Mean lunar radius in kilometers (≈ 1737.4 km). Used for approximate area. */
    private static final double MOON_RADIUS_KM = 1737.4;

    // ---------------------------------------------------------------------
    // Identity / legacy interop
    // ---------------------------------------------------------------------

    /** Optional numeric identifier (non-negative). */
    private int id;

    /** Human-friendly name (e.g., "Near-Side Equatorial", "South Polar Cap"). */
    private String name;

    /** Optional type tag for legacy interop. */
    private ZoneType type;

    /** Optional back-reference to a colony for legacy interop (not used in core math). */
    private Colony colony;

    /** Optional behavior flag used by legacy timePassing() logic. */
    private boolean dynamic;

    /** 0..100 progress value used by legacy code paths. */
    private double growthPercent;

    // ---------------------------------------------------------------------
    // Geometry (stored normalized)
    // ---------------------------------------------------------------------

    /** Southern latitude bound in degrees, in [-90, +90]. */
    private double southLatDeg;

    /** Northern latitude bound in degrees, in [-90, +90]. Must be >= south. */
    private double northLatDeg;

    /**
     * Western longitude bound in degrees, normalized to [-180, +180).
     * May be numerically greater than eastLonDeg when the zone crosses the 180° meridian.
     */
    private double westLonDeg;

    /**
     * Eastern longitude bound in degrees, normalized to [-180, +180).
     * May be numerically less than westLonDeg when the zone crosses the 180° meridian.
     */
    private double eastLonDeg;

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    /** No-arg constructor for serializers/DI. */
    public Zone() {
        this.name = "Zone";
        this.id = 0;
        this.southLatDeg = 0.0;
        this.northLatDeg = 0.0;
        this.westLonDeg = 0.0;
        this.eastLonDeg = 0.0;
        this.growthPercent = 0.0;
        this.dynamic = false;
        this.type = null;
        this.colony = null;
    }

    /**
     * Full constructor (validates/normalizes angles).
     *
     * @param id             optional numeric id (>= 0)
     * @param name           non-null name
     * @param southLatDeg    south latitude (deg, [-90, +90])
     * @param northLatDeg    north latitude (deg, [-90, +90], must be >= south)
     * @param westLonDeg     west longitude (deg, any; normalized to [-180, +180))
     * @param eastLonDeg     east longitude (deg, any; normalized to [-180, +180))
     */
    public Zone(int id,
                String name,
                double southLatDeg,
                double northLatDeg,
                double westLonDeg,
                double eastLonDeg) {
        setId(id);
        setName(name);
        setLatBoundsDeg(southLatDeg, northLatDeg);
        setLonBoundsDeg(westLonDeg, eastLonDeg);
        this.growthPercent = 0.0;
        this.dynamic = false;
        this.type = null;
        this.colony = null;
    }

    /**
     * Legacy constructor used by existing code: register a zone with a type/colony and a flag.
     * Geometry can be set later via setters or builder.
     *
     * @param type     zone type tag (nullable permitted)
     * @param colony   owning colony (nullable permitted)
     * @param dynamic  whether this zone should evolve over time via {@link #timePassing(ClockPulse)}
     */
    public Zone(ZoneType type, Colony colony, boolean dynamic) {
        this();
        this.type = type;
        this.colony = colony;
        this.dynamic = dynamic;
        this.name = (type != null) ? type.name() : this.name;
    }

    // ---------------------------------------------------------------------
    // Builder (immutable-style construction)
    // ---------------------------------------------------------------------

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private int id;
        private final String name;
        private double southLatDeg;
        private double northLatDeg;
        private double westLonDeg;
        private double eastLonDeg;
        private ZoneType type;
        private Colony colony;
        private boolean dynamic;
        private double growthPercent;

        public Builder(String name) {
            this.name = Objects.requireNonNull(name, "name");
            this.id = 0;
            this.southLatDeg = 0.0;
            this.northLatDeg = 0.0;
            this.westLonDeg = 0.0;
            this.eastLonDeg = 0.0;
            this.type = null;
            this.colony = null;
            this.dynamic = false;
            this.growthPercent = 0.0;
        }

        /** Optional, default 0. */
        public Builder id(int v) {
            this.id = v;
            return this;
        }

        /** Optional type tag for legacy interop. */
        public Builder type(ZoneType v) {
            this.type = v;
            return this;
        }

        /** Alias for {@link #type(ZoneType)} to match UI expectations. */
        public Builder zoneType(ZoneType v) {
            this.type = v;
            return this;
        }

        /** Optional colony back-reference. */
        public Builder colony(Colony v) {
            this.colony = v;
            return this;
        }

        /** Whether this zone evolves via {@link #timePassing(ClockPulse)}. */
        public Builder dynamic(boolean v) {
            this.dynamic = v;
            return this;
        }

        /** Pre-set growth percent (0..100). */
        public Builder growthPercent(double v) {
            this.growthPercent = clampPercent(v);
            return this;
        }

        /** Set latitude bounds (deg). */
        public Builder latBoundsDeg(double south, double north) {
            this.southLatDeg = south;
            this.northLatDeg = north;
            return this;
        }

        /** Set longitude bounds (deg, any convention; will be normalized to [-180, +180)). */
        public Builder lonBoundsDeg(double west, double east) {
            this.westLonDeg = west;
            this.eastLonDeg = east;
            return this;
        }

        public Zone build() {
            Zone z = new Zone(id, name, southLatDeg, northLatDeg, westLonDeg, eastLonDeg);
            z.type = this.type;
            z.colony = this.colony;
            z.dynamic = this.dynamic;
            z.growthPercent = this.growthPercent;
            return z;
        }
    }

    // ---------------------------------------------------------------------
    // Copy/with helpers (immutable style)
    // ---------------------------------------------------------------------

    public Zone copy() {
        Zone z = new Zone(id, name, southLatDeg, northLatDeg, westLonDeg, eastLonDeg);
        z.type = this.type;
        z.colony = this.colony;
        z.dynamic = this.dynamic;
        z.growthPercent = this.growthPercent;
        return z;
    }

    public Zone withId(int v) {
        Zone z = copy();
        z.setId(v);
        return z;
    }

    public Zone withName(String v) {
        Zone z = copy();
        z.setName(v);
        return z;
    }

    public Zone withLatBoundsDeg(double south, double north) {
        Zone z = copy();
        z.setLatBoundsDeg(south, north);
        return z;
    }

    public Zone withLonBoundsDeg(double west, double east) {
        Zone z = copy();
        z.setLonBoundsDeg(west, east);
        return z;
    }

    public Zone withType(ZoneType v) {
        Zone z = copy();
        z.type = v;
        return z;
    }

    /** Alias for {@link #withType(ZoneType)}. */
    public Zone withZoneType(ZoneType v) {
        return withType(v);
    }

    public Zone withColony(Colony v) {
        Zone z = copy();
        z.colony = v;
        return z;
    }

    public Zone withDynamic(boolean v) {
        Zone z = copy();
        z.dynamic = v;
        return z;
    }

    public Zone withGrowthPercent(double v) {
        Zone z = copy();
        z.growthPercent = clampPercent(v);
        return z;
    }

    // ---------------------------------------------------------------------
    // Getters (canonical)
    // ---------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Optional type tag for legacy interop. */
    public ZoneType getType() {
        return type;
    }

    /**
     * Alias used by UI layer.
     * @return the zone type (may be null)
     */
    public ZoneType getZoneType() {
        return getType();
    }

    /** Optional colony back-reference. */
    public Colony getColony() {
        return colony;
    }

    /** Whether this zone evolves via {@link #timePassing(ClockPulse)}. */
    public boolean isDynamic() {
        return dynamic;
    }

    /** @return growth percent in [0, 100]. */
    public double getGrowthPercent() {
        return growthPercent;
    }

    /** @return south latitude in degrees [-90, +90]. */
    public double getSouthLatDeg() {
        return southLatDeg;
    }

    /** @return north latitude in degrees [-90, +90]. */
    public double getNorthLatDeg() {
        return northLatDeg;
    }

    /**
     * @return west longitude in degrees normalized to [-180, +180).
     *         May be numerically greater than {@link #getEastLonDeg()} when crossing the dateline.
     */
    public double getWestLonDeg() {
        return westLonDeg;
    }

    /**
     * @return east longitude in degrees normalized to [-180, +180).
     *         May be numerically less than {@link #getWestLonDeg()} when crossing the dateline.
     */
    public double getEastLonDeg() {
        return eastLonDeg;
    }

    // Convenience getters in 0–360° convention (useful for some lunar datasets/tools).
    public double getWestLon360Deg() {
        return normalizeLon360(westLonDeg);
    }

    public double getEastLon360Deg() {
        return normalizeLon360(eastLonDeg);
    }

    /** Center latitude in degrees. */
    public double getCenterLatDeg() {
        return (southLatDeg + northLatDeg) * 0.5;
    }

    /**
     * Center longitude in degrees ([-180, +180)), robust for dateline‑crossing zones.
     * Uses shortest-arc midpoint on the circle.
     */
    public double getCenterLonDeg() {
        double w = westLonDeg;
        double e = eastLonDeg;
        if (!crossesDateline()) {
            return normalizeLon180((w + e) * 0.5);
        }
        double mid = (w + (e + 360.0)) * 0.5;
        return normalizeLon180(mid);
    }

    /** Width in latitude degrees (>= 0). */
    public double getLatSpanDeg() {
        return Math.max(0.0, northLatDeg - southLatDeg);
    }

    /** Width in longitude degrees taking dateline crossings into account (in (0, 360]). */
    public double getLonSpanDeg() {
        double w = westLonDeg;
        double e = eastLonDeg;
        double span = e - w;
        if (span < 0.0) {
            span += 360.0;
        }
        return span;
    }

    // ---------------------------------------------------------------------
    // Setters (validated & normalized). Keep legacy "mutable POJO" usage working.
    // ---------------------------------------------------------------------

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0 but was " + id);
        }
        this.id = id;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /** Set/override the type tag. */
    public void setType(ZoneType type) {
        this.type = type;
    }

    /** Alias for {@link #setType(ZoneType)} to match UI expectations. */
    public void setZoneType(ZoneType type) {
        setType(type);
    }

    /** Set latitude bounds (deg), validating [-90, +90] and that north >= south. */
    public void setLatBoundsDeg(double south, double north) {
        requireLatDeg(south, "southLatDeg");
        requireLatDeg(north, "northLatDeg");
        if (north < south) {
            throw new IllegalArgumentException("northLatDeg must be >= southLatDeg");
        }
        this.southLatDeg = south;
        this.northLatDeg = north;
    }

    /**
     * Set longitude bounds (deg), normalized to [-180, +180).
     * The zone may cross the dateline (westLonDeg &gt; eastLonDeg numerically).
     */
    public void setLonBoundsDeg(double west, double east) {
        this.westLonDeg = normalizeLon180(west);
        this.eastLonDeg = normalizeLon180(east);
    }

    /** Set growth percent in [0, 100]. */
    public void setGrowthPercent(double v) {
        this.growthPercent = clampPercent(v);
    }

    // ---------------------------------------------------------------------
    // Spatial predicates & operations
    // ---------------------------------------------------------------------

    /** @return true if the zone crosses the dateline (i.e., west &gt; east in [-180, +180)). */
    public boolean crossesDateline() {
        return westLonDeg > eastLonDeg;
    }

    /**
     * Returns true if the given point (lat, lon) lies inside this zone (inclusive),
     * handling dateline crossing robustly.
     *
     * @param latDeg latitude in degrees
     * @param lonDeg longitude in degrees (any; normalized internally)
     */
    public boolean contains(double latDeg, double lonDeg) {
        double lat = latDeg;
        requireLatDeg(lat, "latDeg");
        double lon = normalizeLon180(lonDeg);

        boolean inLat = (lat >= southLatDeg) && (lat <= northLatDeg);
        boolean inLon;
        if (!crossesDateline()) {
            inLon = (lon >= westLonDeg) && (lon <= eastLonDeg);
        } else {
            inLon = (lon >= westLonDeg) || (lon <= eastLonDeg);
        }
        return inLat && inLon;
    }

    /**
     * Returns true if this zone intersects another zone (non-empty overlap).
     * Dateline-aware in longitude.
     */
    public boolean intersects(Zone other) {
        Objects.requireNonNull(other, "other");
        boolean latOverlap = !(this.northLatDeg < other.southLatDeg || other.northLatDeg < this.southLatDeg);
        if (!latOverlap) {
            return false;
        }
        double[][] aIntervals = lonIntervals(this.westLonDeg, this.eastLonDeg);
        double[][] bIntervals = lonIntervals(other.westLonDeg, other.eastLonDeg);
        for (double[] a : aIntervals) {
            for (double[] b : bIntervals) {
                if (intervalsOverlap(a[0], a[1], b[0], b[1])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a new zone grown by the given padding in degrees (non-negative).
     * Latitudes are clamped to [-90, +90]. Longitudes expand respecting dateline logic.
     */
    public Zone expandedBy(double latPaddingDeg, double lonPaddingDeg) {
        if (latPaddingDeg < 0.0 || lonPaddingDeg < 0.0) {
            throw new IllegalArgumentException("padding must be non-negative");
        }
        double south = clampLat(southLatDeg - latPaddingDeg);
        double north = clampLat(northLatDeg + latPaddingDeg);
        double west = normalizeLon180(westLonDeg - lonPaddingDeg);
        double east = normalizeLon180(eastLonDeg + lonPaddingDeg);
        return withLatBoundsDeg(south, north).withLonBoundsDeg(west, east);
    }

    // ---------------------------------------------------------------------
    // Legacy methods expected by existing callers
    // ---------------------------------------------------------------------

    /**
     * Legacy tick hook invoked by the simulation.
     * This implementation advances {@link #growthPercent} slowly when {@link #dynamic} is true.
     *
     * @param pulse clock pulse (ignored for now to keep behavior conservative)
     */
    public void timePassing(ClockPulse pulse) {
        if (dynamic) {
            double next = this.growthPercent + 0.05;
            this.growthPercent = clampPercent(next);
        }
    }

    /**
     * Approximate surface area of the zone on a sphere using the mean lunar radius.
     * Result is in square kilometers.
     *
     * @return area in km^2
     */
    public double getArea() {
        double lat1 = Math.toRadians(southLatDeg);
        double lat2 = Math.toRadians(northLatDeg);
        double dLon = Math.toRadians(getLonSpanDeg());
        double band = Math.abs(Math.sin(lat2) - Math.sin(lat1));
        double r2 = MOON_RADIUS_KM * MOON_RADIUS_KM;
        return r2 * band * dLon;
    }

    // ---------------------------------------------------------------------
    // Equality, hash, string (value semantics by id+name+bounds+type)
    // ---------------------------------------------------------------------

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Zone)) {
            return false;
        }
        Zone z = (Zone) other;
        return this.id == z.id
                && Objects.equals(this.name, z.name)
                && Objects.equals(this.type, z.type)
                && Double.doubleToLongBits(this.southLatDeg) == Double.doubleToLongBits(z.southLatDeg)
                && Double.doubleToLongBits(this.northLatDeg) == Double.doubleToLongBits(z.northLatDeg)
                && Double.doubleToLongBits(this.westLonDeg) == Double.doubleToLongBits(z.westLonDeg)
                && Double.doubleToLongBits(this.eastLonDeg) == Double.doubleToLongBits(z.eastLonDeg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                name,
                type,
                Double.doubleToLongBits(southLatDeg),
                Double.doubleToLongBits(northLatDeg),
                Double.doubleToLongBits(westLonDeg),
                Double.doubleToLongBits(eastLonDeg));
    }

    @Override
    public String toString() {
        return "Zone[id=" + id
                + ", name=" + name
                + ", type=" + type
                + ", south=" + southLatDeg
                + "°, north=" + northLatDeg
                + "°, west=" + westLonDeg
                + "°, east=" + eastLonDeg
                + "°, growth=" + growthPercent
                + "%]";
    }

    // ---------------------------------------------------------------------
    // Utilities (normalization & validation)
    // ---------------------------------------------------------------------

    /** Normalize longitude to [-180, +180). */
    public static double normalizeLon180(double lonDeg) {
        double x = lonDeg % 360.0;
        if (x < -180.0) {
            x += 360.0;
        } else if (x >= 180.0) {
            x -= 360.0;
        }
        return x;
    }

    /** Normalize longitude to [0, 360). */
    public static double normalizeLon360(double lonDeg) {
        double x = lonDeg % 360.0;
        if (x < 0.0) {
            x += 360.0;
        }
        return x;
    }

    private static double clampLat(double latDeg) {
        if (latDeg > 90.0) {
            return 90.0;
        }
        if (latDeg < -90.0) {
            return -90.0;
        }
        return latDeg;
    }

    private static void requireLatDeg(double latDeg, String field) {
        boolean invalid = Double.isNaN(latDeg) || Double.isInfinite(latDeg)
                || latDeg < -90.0 || latDeg > 90.0;
        if (invalid) {
            throw new IllegalArgumentException(field + " must be finite and within [-90, +90]: " + latDeg);
        }
    }

    private static boolean intervalsOverlap(double aStart, double aEnd, double bStart, double bEnd) {
        return !(aEnd < bStart || bEnd < aStart);
    }

    /**
     * Returns up to two non-crossing longitude intervals in [-180, +180) for a possibly
     * dateline-crossing span. Each interval is [start, end].
     */
    private static double[][] lonIntervals(double west, double east) {
        if (west <= east) {
            return new double[][] { { west, east } };
        }
        return new double[][] { { west, 180.0 }, { -180.0, east } };
    }

    private static double clampPercent(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return 0.0;
        }
        if (v < 0.0) {
            return 0.0;
        }
        if (v > 100.0) {
            return 100.0;
        }
        return v;
    }

    // ---------------------------------------------------------------------
    // Convenience presets (optional; safe to ignore)
    // ---------------------------------------------------------------------

    /** Near-side global zone (lat: -90..+90, lon: -90..+90 approx.). */
    public static Zone nearSide() {
        return Zone.builder("Near-Side")
                .id(1)
                .latBoundsDeg(-90.0, 90.0)
                .lonBoundsDeg(-90.0, 90.0)
                .build();
    }

    /** Far-side global zone (lat: -90..+90, lon: +90..-90 across dateline). */
    public static Zone farSide() {
        return Zone.builder("Far-Side")
                .id(2)
                .latBoundsDeg(-90.0, 90.0)
                .lonBoundsDeg(90.0, -90.0)
                .build();
    }

    /** South polar cap example (lat &lt;= -80°). */
    public static Zone southPolarCap() {
        return Zone.builder("South Polar Cap")
                .id(3)
                .latBoundsDeg(-90.0, -80.0)
                .lonBoundsDeg(-180.0, 180.0)
                .build();
    }

    /** North polar cap example (lat &gt;= +80°). */
    public static Zone northPolarCap() {
        return Zone.builder("North Polar Cap")
                .id(4)
                .latBoundsDeg(80.0, 90.0)
                .lonBoundsDeg(-180.0, 180.0)
                .build();
    }
}
