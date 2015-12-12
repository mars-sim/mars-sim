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

import java.awt.geom.Point2D;


/**
 * A set of handy methods that will be used all over
 * the place.
 * @author hansolo
 */
public enum PoiUtil {

    INSTANCE;

    // <editor-fold defaultstate="collapsed" desc="POI related utils">
    //********************************** POI related utils *************************************************************
    /**
     * Returns the distance in meters between two given points of interest, P1 and P2
     * on our lovely planet earth.
     * @param P1
     * @param P2
     * @return The distance between P1 and P2 in meter
     */
    public double distanceTo(final Poi P1, final Poi P2) {
        return distanceTo(P1.getLon(), P1.getLat(), P2.getLon(), P2.getLat());
    }

    /**
     * Returns the distance in meters between two given points defined by their coordinates
     * LAT1, LON1 and LAT2, LON2. The radius of our lovely planet earth will be taken into
     * account which means the distance is not calculated like a straight line but more like
     * a curve.
     * @param LAT1
     * @param LON1
     * @param LAT2
     * @param LON2
     * @return The distance between P1 and P2 defined by their lat, lon values in meter
     */
    public double distanceTo(final double LAT1, final double LON1, final double LAT2, final double LON2) {
        final double EARTH_RADIUS = 6371000.0; // m
        return Math.abs(Math.acos(Math.sin(Math.toRadians(LAT1)) * Math.sin(Math.toRadians(LAT2)) + Math.cos(Math.toRadians(LAT1)) * Math.cos(Math.toRadians(LAT2)) * Math.cos(Math.toRadians(LON1 - LON2))) * EARTH_RADIUS);
    }

    /**
     * Returns a new point of interest which position is calculated by the given point of interest
     * moved by the given DISTANCE in the direction defined by the given ANGLE.
     * @param POINT
     * @param DISTANCE
     * @param ANGLE
     * @return Poi that was calculated by moving the given point about the given distance and angle
     */
    public Poi shiftTo(final Poi POINT, final double DISTANCE, final double ANGLE) {
        final double EARTH_RADIUS = 6371000.0; // m
        final double LON1 = Math.toRadians(POINT.getLon());
        final double LAT1 = Math.toRadians(POINT.getLat());
        final double LAT2 = Math.asin(Math.sin(LAT1) * Math.cos(DISTANCE / EARTH_RADIUS) + Math.cos(LAT1) * Math.sin(DISTANCE / EARTH_RADIUS) * Math.cos(Math.toRadians(ANGLE)));
        final double LON2 = LON1 + Math.atan2(Math.sin(Math.toRadians(ANGLE)) * Math.sin(DISTANCE / EARTH_RADIUS) * Math.cos(LAT1), Math.cos(DISTANCE / EARTH_RADIUS) - Math.sin(LAT1) * Math.sin(LAT2));
        final double LON2_CORRECTED = (LON2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI; //normalise to -180...+180

        return new Poi(POINT.getName(), Math.toDegrees(LON2_CORRECTED), Math.toDegrees(LAT2));
    }

    /**
     * Returns a new point of interest with the given NAME which position is defined by the given
     * location in LAT1, LON1 and the DISTANCE to move from that position the in the direction
     * defined by the given ANGLE
     * @param NAME
     * @param LAT1
     * @param LON1
     * @param DISTANCE
     * @param ANGLE
     * @return Poi that was calculated by moving the given point about the given distance and angle
     */
    public Poi shiftTo(final String NAME, final double LAT1, final double LON1, final double DISTANCE,
                       final double ANGLE) {
        return shiftTo(new Poi(NAME, LAT1, LON1), DISTANCE, ANGLE);
    }

    /**
     * Returns a point which represents the x,y position on the given rectangle MAP of the given
     * location in LAT,LON coordinates
     * @param LAT
     * @param LON
     * @param MAP
     * @return Point2D that represents the x,y position of the given lat, lon values on the
     * given rectangle of the map
     */
    public Point2D toXY(final double LAT, final double LON, final java.awt.Rectangle MAP) {
        final double LATITUDE = (LAT * (-1)) + 90.0;
        final double LONGITUDE = LON + 180.0;

        final double X = Math.round(LONGITUDE * (MAP.getWidth() / 360));
        final double Y = Math.round(LATITUDE * (MAP.getHeight() / 180));

        return new Point2D.Double(X, Y);
    }

    /**
     * Returns a point which represents the x,y position on the given rectangle MAP of the given point
     * of interest POINT
     * @param POINT
     * @param MAP
     * @return Point2D that represents the x,y position of the given lat, lon values on the
     * given rectangle of the map
     */
    public Point2D toXY(final Poi POINT, final java.awt.Rectangle MAP) {
        return toXY(POINT.getLat(), POINT.getLon(), MAP);
    }
    // </editor-fold>
}
