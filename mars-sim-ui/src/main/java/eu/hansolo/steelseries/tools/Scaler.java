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
package eu.hansolo.steelseries.tools;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public enum Scaler {

    INSTANCE;

    /**
     * Returns a double that represents the area of the given point array of a polygon
     * @param POLYGON
     * @param N
     * @return a double that represents the area of the given point array of a polygon
     */
    private double calcSignedPolygonArea(final Point2D[] POLYGON) {
        final int N = POLYGON.length;
        int i;
        int j;
        double area = 0;

        for (i = 0; i < N; i++) {
            j = (i + 1) % N;
            area += POLYGON[i].getX() * POLYGON[j].getY();
            area -= POLYGON[i].getY() * POLYGON[j].getX();
        }
        area /= 2.0;

        return (area);
        //return(area < 0 ? -area : area); for unsigned
    }

    /**
     * Returns a Point2D object that represents the center of mass of the given point array which represents a
     * polygon.
     * @param POLYGON
     * @return a Point2D object that represents the center of mass of the given point array
     */
    public Point2D calcCenterOfMass(final Point2D[] POLYGON) {
        final int N = POLYGON.length;
        double cx = 0;
        double cy = 0;
        double area = calcSignedPolygonArea(POLYGON);
        final Point2D CENTROID = new Point2D.Double();
        int i;
        int j;

        double factor = 0;
        for (i = 0; i < N; i++) {
            j = (i + 1) % N;
            factor = (POLYGON[i].getX() * POLYGON[j].getY() - POLYGON[j].getX() * POLYGON[i].getY());
            cx += (POLYGON[i].getX() + POLYGON[j].getX()) * factor;
            cy += (POLYGON[i].getY() + POLYGON[j].getY()) * factor;
        }
        area *= 6.0f;
        factor = 1 / area;
        cx *= factor;
        cy *= factor;

        CENTROID.setLocation(cx, cy);
        return CENTROID;
    }

    /**
     * Returns an array of all points of the given shape
     * @param SHAPE
     * @return an array of all points of the given shape
     */
    public Point2D[] getPointArrayOfShape(final Shape SHAPE) {
        ArrayList<Point2D> pointList = new ArrayList<Point2D>(32);
        final PathIterator PATH_ITERATOR = SHAPE.getPathIterator(null);
        int lastMoveToIndex = -1;
        while (!PATH_ITERATOR.isDone()) {
            final double[] COORDINATES = new double[6];
            switch (PATH_ITERATOR.currentSegment(COORDINATES)) {
                case PathIterator.SEG_MOVETO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    lastMoveToIndex++;
                    break;
                case PathIterator.SEG_LINETO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    pointList.add(new Point2D.Double(COORDINATES[2], COORDINATES[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    pointList.add(new Point2D.Double(COORDINATES[2], COORDINATES[3]));
                    pointList.add(new Point2D.Double(COORDINATES[4], COORDINATES[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    if (lastMoveToIndex >= 0) {
                        pointList.add(pointList.get(lastMoveToIndex));
                    }
                    break;
            }
            PATH_ITERATOR.next();
        }
        final Point2D[] POINT_ARRAY = new Point2D[pointList.size()];
        return POINT_ARRAY;
    }

    /**
     * Returns a Point2D object that represents the center of mass of the given shape.
     * @param SHAPE
     * @return a Point2D object that represents the center of mass of the given shape
     */
    public Point2D getCentroid(final Shape SHAPE) {
        ArrayList<Point2D> pointList = new ArrayList<Point2D>(32);
        final PathIterator PATH_ITERATOR = SHAPE.getPathIterator(null);
        int lastMoveToIndex = -1;
        while (!PATH_ITERATOR.isDone()) {
            final double[] COORDINATES = new double[6];
            switch (PATH_ITERATOR.currentSegment(COORDINATES)) {
                case PathIterator.SEG_MOVETO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    lastMoveToIndex++;
                    break;
                case PathIterator.SEG_LINETO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    pointList.add(new Point2D.Double(COORDINATES[2], COORDINATES[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    pointList.add(new Point2D.Double(COORDINATES[0], COORDINATES[1]));
                    pointList.add(new Point2D.Double(COORDINATES[2], COORDINATES[3]));
                    pointList.add(new Point2D.Double(COORDINATES[4], COORDINATES[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    if (lastMoveToIndex >= 0) {
                        pointList.add(pointList.get(lastMoveToIndex));
                    }
                    break;
            }
            PATH_ITERATOR.next();
        }
        final Point2D[] POINT_ARRAY = new Point2D[pointList.size()];
        pointList.toArray(POINT_ARRAY);
        return (calcCenterOfMass(POINT_ARRAY));
    }

    /**
     * Returns a Point2D object that represents the center of mass of the given point array
     * @param POINT_ARRAY
     * @return a Point2D object that represents the center of mass of the given point array
     */
    public Point2D getCentroid(final Point2D[] POINT_ARRAY) {
        return calcCenterOfMass(POINT_ARRAY);
    }

    /**
     * Returns a scaled version of the given convex shape, calculated by the given scale factor.
     * The scaling will be calculated around the centroid of the shape.
     * @param SHAPE
     * @param SCALE_FACTOR
     * @return a scaled version of the given convex shape, calculated around the centroid by the given scale factor.
     */
    public java.awt.Shape scale(final Shape SHAPE, final double SCALE_FACTOR) {
        final Point2D CENTROID = getCentroid(SHAPE);
        final AffineTransform TRANSFORM = AffineTransform.getTranslateInstance((1.0 - SCALE_FACTOR) * CENTROID.getX(), (1.0 - SCALE_FACTOR) * CENTROID.getY());
        TRANSFORM.scale(SCALE_FACTOR, SCALE_FACTOR);
        return TRANSFORM.createTransformedShape(SHAPE);
    }

    /**
     * Returns a scaled version of the given convex shape, calculated by the given scale factor.
     * The scaling will be calculated around the given point.
     * @param SHAPE
     * @param SCALE_FACTOR
     * @param SCALE_CENTER
     * @return a scaled version of the given convex shape, calculated around the given point with the given scale factor.
     */
    public java.awt.Shape scale(final Shape SHAPE, final double SCALE_FACTOR, final Point2D SCALE_CENTER) {
        final AffineTransform TRANSFORM = AffineTransform.getTranslateInstance((1.0 - SCALE_FACTOR) * SCALE_CENTER.getX(), (1.0 - SCALE_FACTOR) * SCALE_CENTER.getY());
        TRANSFORM.scale(SCALE_FACTOR, SCALE_FACTOR);
        return TRANSFORM.createTransformedShape(SHAPE);
    }
}
