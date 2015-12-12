/*
 * Copyright (c) 2012, Gerrit Grunwald
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.hansolo.steelseries.extras;

import eu.hansolo.steelseries.tools.Util;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


/**
 * A class that holds point of interest in latitude and longitude
 * coordinates. Instances of this class will be used only in the
 * radar component.
 * @author hansolo
 */
public class Poi {
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private final Util UTIL = Util.INSTANCE;
    private final String NAME;
    private double lat = 0;
    private double lon = 0;
    private String latDirection = "N";
    private String lonDirection = "W";
    private final Rectangle WORLD_MAP = new Rectangle(0, 0, 40000, 20000);
    private final Point2D LOCATION = new Point2D.Double(lon, lat);
    private Point2D LOCATION_XY = new Point2D.Double(0, 0);
    private final BufferedImage POI_IMAGE = create_POI_Image(5);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Defines a point of interest with the latitude and longitude
     * of 0 which is somewhere in the atlanic ocean between south africa
     * and america.
     * @param NAME
     */
    public Poi(final String NAME) {
        this(NAME, 0, 0);
    }

    /**
     * Defines a point of interest with the given name and the
     * given coordinates as latitude and longitude in the format
     * decimal degrees e.g. 51.485605, 7.479544
     * @param NAME
     * @param LAT
     * @param LON
     */
    public Poi(final String NAME, final double LAT, final double LON) {
        // LAT = Y, LON = X
        this.NAME = NAME;
        this.lat = LAT;
        this.lon = LON;
        setLocation(LAT, LON);
        adjustDirection();
    }

    /**
     * Defines a point of interest with the given name and the
     * given coordinates as latitude and longitude in the format
     * 51° 20' 10"
     * @param NAME
     * @param LAT_DEG
     * @param LAT_MIN
     * @param LAT_SEC
     * @param LON_DEG
     * @param LON_MIN
     * @param LON_SEC
     */
    public Poi(final String NAME, final int LAT_DEG, final int LAT_MIN, final int LAT_SEC, final int LON_DEG,
               final int LON_MIN, final int LON_SEC) {
        this.NAME = NAME;
        this.lat = convert(LAT_DEG, LAT_MIN, LAT_SEC);
        this.lon = convert(LON_DEG, LON_MIN, LON_SEC);
        setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Defines a point of interest with the given name and the
     * given coordinates as latitude and longitude in the format
     * 51° 20.324'
     * @param NAME
     * @param LAT_DEG
     * @param LAT_MIN
     * @param LON_DEG
     * @param LON_MIN
     */
    public Poi(final String NAME, final int LAT_DEG, final double LAT_MIN, final int LON_DEG, final double LON_MIN) {
        this.NAME = NAME;
        this.lat = convert(LAT_DEG, LAT_MIN);
        this.lon = convert(LON_DEG, LON_MIN);
        setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Defines a point of interest with the given name and the
     * given coordinates as latitude and longitude in the format
     * N 51° 20.213'
     * @param NAME
     * @param LAT_DIRECTION
     * @param LAT_DEG
     * @param LAT_MIN
     * @param LON_DIRECTION
     * @param LON_DEG
     * @param LON_MIN
     */
    public Poi(final String NAME, final String LAT_DIRECTION, final int LAT_DEG, final double LAT_MIN,
               final String LON_DIRECTION, final int LON_DEG, final double LON_MIN) {
        this.NAME = NAME;
        this.lat = convert(LAT_DEG, LAT_MIN);
        this.lon = convert(LON_DEG, LON_MIN);
        if (LAT_DIRECTION.equalsIgnoreCase("S")) {
            this.lat *= (-1);
        }
        if (LON_DIRECTION.equalsIgnoreCase("W")) {
            this.lon *= (-1);
        }
        setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Defines a point of interest with the given name and the
     * given coordinates as latitude and longitude in the format
     * N 51° 20' 12"
     * @param NAME
     * @param LAT_DIRECTION
     * @param LAT_DEG
     * @param LAT_MIN
     * @param LAT_SEC
     * @param LON_DIRECTION
     * @param LON_DEG
     * @param LON_MIN
     * @param LON_SEC
     */
    public Poi(final String NAME, final String LAT_DIRECTION, final int LAT_DEG, final int LAT_MIN, final int LAT_SEC,
               final String LON_DIRECTION, final int LON_DEG, final int LON_MIN, final int LON_SEC) {
        this.NAME = NAME;
        this.lat = convert(LAT_DEG, LAT_MIN, LAT_SEC);
        this.lon = convert(LON_DEG, LON_MIN, LON_SEC);
        if (LAT_DIRECTION.equalsIgnoreCase("S")) {
            this.lat *= (-1);
        }
        if (LON_DIRECTION.equalsIgnoreCase("W")) {
            this.lon *= (-1);
        }
        setLocation(this.lat, this.lon);
        adjustDirection();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Returns the name of the point of interest
     * @return the name of the point of interest
     */
    public String getName() {
        return this.NAME;
    }

    /**
     * Returns the image of the point of interest. This
     * is for future use, e.g. for location specific
     * images.
     * @return a buffered image that will be used to
     * visualize the point of interest
     */
    public BufferedImage getPoiImage() {
        return this.POI_IMAGE;
    }

    /**
     * Returns the longitude of the poi in the format
     * 51.123124
     * @return the longitude of the poi in the format 51.1231
     */
    public double getLon() {
        return this.lon;
    }

    /**
     * Sets the longitude of the poi in the format
     * 51.12312
     * @param LON
     */
    public void setLon(final double LON) {
        this.lon = LON;
        this.LOCATION.setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Sets the longitude of the poi in the format
     * 51° 20' 10"
     * @param LON_DEG
     * @param LON_MIN
     * @param LON_SEC
     */
    public void setLon(final int LON_DEG, final int LON_MIN, final int LON_SEC) {
        this.lon = convert(LON_DEG, LON_MIN, LON_SEC);
        this.LOCATION.setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Sets the longitude of the poi in the format
     * 51° 20.12'
     * @param LON_DEG
     * @param LON_MIN
     */
    public void setLon(final int LON_DEG, final double LON_MIN) {
        this.lon = convert(LON_DEG, LON_MIN);
        this.LOCATION.setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Returns the latitude of the poi in the format
     * 7.1231
     * @return the latitude of the poi  in the format 7.124
     */
    public double getLat() {
        return this.lat;
    }

    /**
     * Sets the latitude of the poi in the format
     * 7.135
     * @param LAT
     */
    public void setLat(final double LAT) {
        this.lat = LAT;
        this.LOCATION.setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Sets the latitude of the poi in the format
     * 7° 5' 20"
     * @param LAT_DEG
     * @param LAT_MIN
     * @param LAT_SEC
     */
    public void setLat(final int LAT_DEG, final int LAT_MIN, final int LAT_SEC) {
        this.lat = convert(LAT_DEG, LAT_MIN, LAT_SEC);
        this.LOCATION.setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Sets the latitude of the poi in the format
     * 7° 20.123'
     * @param LAT_DEG
     * @param LAT_MIN
     */
    public void setLat(final int LAT_DEG, final double LAT_MIN) {
        this.lat = convert(LAT_DEG, LAT_MIN);
        this.LOCATION.setLocation(this.lat, this.lon);
        adjustDirection();
    }

    /**
     * Returns the direction of the longitude in the form
     * of E and W
     * @return returns E or W
     */
    public String getLonDirection() {
        return this.lonDirection;
    }

    /**
     * Returns the direction of the latitude in the form
     * of N and S
     * @return returns N or S
     */
    public String getLatDirection() {
        return this.latDirection;
    }

    /**
     * Returns the location of the poi as Point2D
     * @return the location of the poi
     */
    public Point2D getLocation() {
        return this.LOCATION;
    }

    /**
     * Sets the location of the poi by the given Point2D
     * @param LOCATION
     */
    public void setLocation(final Point2D LOCATION) {
        this.lon = LOCATION.getX();
        this.lat = LOCATION.getY();
        this.LOCATION.setLocation(LOCATION);
        this.LOCATION_XY.setLocation(toXY(this.lat, this.lon));
        adjustDirection();
    }

    /**
     * Sets the location of the poi by the given latitude
     * and longitude values
     * @param LAT
     * @param LON
     */
    public final void setLocation(final double LAT, final double LON) {
        this.lon = LON; // X
        this.lat = LAT; // Y
        this.LOCATION.setLocation(LON, LAT);
        this.LOCATION_XY.setLocation(toXY(LAT, LON));
        adjustDirection();
    }

    /**
     * Returns the x,y coordinates of the poi
     * @return x,y coordinates of the poi
     */
    public Point2D getLocationXY() {
        return this.LOCATION_XY;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Misc">
    /**
     * Returns the distance of the poi to the given poi in meters.
     * The calculation takes the earth radius into account.
     * @param POINT_OF_INTEREST
     * @return the distance in meters to the given poi
     */
    public double distanceTo(final Poi POINT_OF_INTEREST) {
        return distanceTo(POINT_OF_INTEREST.getLat(), POINT_OF_INTEREST.getLon());
    }

    /**
     * Returns the distance in meters of the poi to the coordinate defined
     * by the given latitude and longitude. The calculation takes the
     * earth radius into account.
     * @param LAT
     * @param LON
     * @return the distance in meters to the given coordinate
     */
    public double distanceTo(final double LAT, final double LON) {
        final double EARTH_RADIUS = 6371000.0; // m
        return Math.abs(Math.acos(Math.sin(Math.toRadians(LAT)) * Math.sin(Math.toRadians(this.lat)) + Math.cos(Math.toRadians(LAT)) * Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(LON - this.lon))) * EARTH_RADIUS);
    }

    /**
     * Moves the poi to the position defined by the given distance and angle
     * @param DISTANCE
     * @param ANGLE
     * @return the poi moved by the given distance and angle
     */
    public Point2D shiftTo(final double DISTANCE, final double ANGLE) {
        final double EARTH_RADIUS = 6371000.0; // m
        final double LON1 = Math.toRadians(this.lon);
        final double LAT1 = Math.toRadians(this.lat);
        final double LAT2 = Math.asin(Math.sin(LAT1) * Math.cos(DISTANCE / EARTH_RADIUS) + Math.cos(LAT1) * Math.sin(DISTANCE / EARTH_RADIUS) * Math.cos(Math.toRadians(ANGLE)));
        final double LON2 = LON1 + Math.atan2(Math.sin(Math.toRadians(ANGLE)) * Math.sin(DISTANCE / EARTH_RADIUS) * Math.cos(LAT1), Math.cos(DISTANCE / EARTH_RADIUS) - Math.sin(LAT1) * Math.sin(LAT2));
        final double LON2_CORRECTED = (LON2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI; //normalise to -180...+180

        setLocation(Math.toDegrees(LAT2), Math.toDegrees(LON2_CORRECTED));

        return getLocation();
    }

    /**
     * Defines the direction by the current location of the poi
     */
    private void adjustDirection() {
        if (this.lat > 0) {
            this.latDirection = "N";
        } else {
            this.latDirection = "S";
        }

        if (this.lon > 0) {
            this.lonDirection = "E";
        } else {
            this.lonDirection = "W";
        }
    }

    /**
     * Converts coordinates from DDD° 'MM "SS to DDD.DDDDDD°
     * @param DEG
     * @param MIN
     * @param SEC
     * @return coordinate in the format DDD.DDDDDD°
     */
    private double convert(final int DEG, final int MIN, final int SEC) {
        return DEG + (MIN / 60) + (SEC / 3600);
    }

    /**
     * Converts coordinates from DDD° MM.MMMM' to DDD.DDDDDD°
     * @param DEG
     * @param MIN
     * @return coordinate in the format DDD.DDDDDD°
     */
    private double convert(final int DEG, final double MIN) {
        return DEG + (MIN / 60);
    }

    /**
     * Converts the given latitude and longitude to x,y values
     * @param LAT
     * @param LON
     * @return Point2D with the location of the given lat, lon
     */
    public final Point2D toXY(final double LAT, final double LON) {
        final double LATITUDE = (LAT * (-1)) + 90.0;
        final double LONGITUDE = LON + 180.0;

        final double X = Math.round(LONGITUDE * (WORLD_MAP.getWidth() / 360));
        final double Y = Math.round(LATITUDE * (WORLD_MAP.getHeight() / 180));

        return new java.awt.geom.Point2D.Double(X, Y);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image related">
    /**
     * Creates the image of the poi
     * @param WIDTH
     * @return buffered image of the poi
     */
    private BufferedImage create_POI_Image(final int WIDTH) {
        if (WIDTH <= 0) {
            return null;
        }

        final java.awt.image.BufferedImage IMAGE = UTIL.createImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_NORMALIZE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final java.awt.geom.Ellipse2D BLIP = new java.awt.geom.Ellipse2D.Double(0, 0, WIDTH, WIDTH);
        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(BLIP.getCenterX(), BLIP.getCenterY());
        final float[] FRACTIONS = {
            0.0f,
            1.0f
        };
        final Color[] COLORS = {
            new Color(1.0f, 1.0f, 1.0f, 0.9f),
            new Color(1.0f, 1.0f, 1.0f, 0.0f)
        };
        final java.awt.RadialGradientPaint GRADIENT = new java.awt.RadialGradientPaint(CENTER, (int) (WIDTH / 2.0), FRACTIONS, COLORS);
        G2.setPaint(GRADIENT);
        G2.fill(BLIP);

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return (getLat() + "," + getLon());
    }
}
