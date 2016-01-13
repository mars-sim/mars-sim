package org.mars_sim.msp.ui.javafx;

import static java.util.Objects.requireNonNull;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import org.apache.commons.math3.util.Precision;

/**
 * Replacement to {@link javafx.geometry.Bounds} with observable coords.
 * <p>
 * The class offers following properties to observe:<br>
 * <ul>
 * <li>{@link #minX}
 * <li>{@link #minY}
 * <li>{@link #minZ}
 * <li>{@link #centerX}
 * <li>{@link #centerY}
 * <li>{@link #centerZ}
 * <li>{@link #maxX}
 * <li>{@link #maxY}
 * <li>{@link #maxZ}
 * <li>{@link #width}
 * <li>{@link #height}
 * <li>{@link #depth}
 * </ul>
 *
 * @author dejv78 (dejv78.github.io)
 * @since 1.0.0
 */
public class ObservableBounds {

    protected final DoubleProperty minX = new SimpleDoubleProperty(this, "minX");
    protected final DoubleProperty maxX = new SimpleDoubleProperty(this, "maxX");
    protected final DoubleProperty minY = new SimpleDoubleProperty(this, "minY");
    protected final DoubleProperty maxY = new SimpleDoubleProperty(this, "maxY");
    protected final DoubleProperty minZ = new SimpleDoubleProperty(this, "minZ");
    protected final DoubleProperty maxZ = new SimpleDoubleProperty(this, "maxZ");

    protected final DoubleProperty centerX = new SimpleDoubleProperty(this, "centerX");
    protected final DoubleProperty centerY = new SimpleDoubleProperty(this, "centerY");
    protected final DoubleProperty centerZ = new SimpleDoubleProperty(this, "centerZ");

    protected final DoubleProperty width = new SimpleDoubleProperty(this, "width");
    protected final DoubleProperty height = new SimpleDoubleProperty(this, "height");
    protected final DoubleProperty depth = new SimpleDoubleProperty(this, "depth");


    public ObservableBounds() {
        width.bind(maxX.subtract(minX));
        height.bind(maxY.subtract(minY));
        depth.bind(maxZ.subtract(minZ));
        centerX.bind(minX.add(width.divide(2.0d)));
        centerY.bind(minY.add(height.divide(2.0d)));
        centerZ.bind(minZ.add(depth.divide(2.0d)));
    }


    public ObservableBounds(DoubleExpression minX, DoubleExpression minY, DoubleExpression maxX, DoubleExpression maxY) {
        this();
        requireNonNull(minX, "Parameter 'minX' is null");
        requireNonNull(minY, "Parameter 'minY' is null");
        requireNonNull(maxX, "Parameter 'maxX' is null");
        requireNonNull(maxY, "Parameter 'maxY' is null");

        this.minX.bind(minX);
        this.minY.bind(minY);
        this.maxX.bind(maxX);
        this.maxY.bind(maxY);
    }


    public ObservableBounds(DoubleExpression minX, DoubleExpression minY, DoubleExpression minZ, DoubleExpression maxX, DoubleExpression maxY, DoubleExpression maxZ) {
        this(minX, minY, maxX, maxY);
        requireNonNull(minZ, "Parameter 'minZ' is null");
        requireNonNull(maxZ, "Parameter 'maxZ' is null");

        this.minZ.bind(minZ);
        this.maxZ.bind(maxZ);
    }


    public double getMinX() {
        return minX.get();
    }


    public void setMinX(double minX) {
        this.minX.set(minX);
    }


    public double getCenterX() {
        return centerX.get();
    }


    public double getMaxX() {
        return maxX.get();
    }


    public void setMaxX(double maxX) {
        this.maxX.set(maxX);
    }


    public double getMinY() {
        return minY.get();
    }


    public void setMinY(double minY) {
        this.minY.set(minY);
    }


    public double getCenterY() {
        return centerY.get();
    }


    public double getMaxY() {
        return maxY.get();
    }


    public void setMaxY(double maxY) {
        this.maxY.set(maxY);
    }


    public double getMinZ() {
        return minZ.get();
    }


    public void setMinZ(double minZ) {
        this.minZ.set(minZ);
    }


    public double getCenterZ() {
        return centerZ.get();
    }


    public double getMaxZ() {
        return maxZ.get();
    }


    public void setMaxZ(double maxZ) {
        this.maxZ.set(maxZ);
    }


    public double getWidth() {
        return width.get();
    }


    public double getHeight() {
        return height.get();
    }


    public double getDepth() {
        return depth.get();
    }


    public DoubleProperty minXProperty() {
        return minX;
    }


    public ReadOnlyDoubleProperty centerXProperty() {
        return centerX;
    }


    public DoubleProperty maxXProperty() {
        return maxX;
    }


    public DoubleProperty minYProperty() {
        return minY;
    }


    public ReadOnlyDoubleProperty centerYProperty() {
        return centerY;
    }


    public DoubleProperty maxYProperty() {
        return maxY;
    }


    public DoubleProperty minZProperty() {
        return minZ;
    }


    public ReadOnlyDoubleProperty centerZProperty() {
        return centerZ;
    }


    public DoubleProperty maxZProperty() {
        return maxZ;
    }


    public ReadOnlyDoubleProperty widthProperty() {
        return width;
    }


    public ReadOnlyDoubleProperty heightProperty() {
        return height;
    }


    public ReadOnlyDoubleProperty depthProperty() {
        return depth;
    }


    public BooleanBinding isEmpty() {
        return width.isNotEqualTo(0, Precision.EPSILON).and(height.isNotEqualTo(0, Precision.EPSILON)).and(depth.isNotEqualTo(0, Precision.EPSILON));
    }


    public BooleanBinding contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }


    public BooleanBinding contains(Point3D p) {
        return contains(p.getX(), p.getY(), p.getZ());
    }


    public BooleanBinding contains(double x, double y) {
        return minX.lessThanOrEqualTo(x)
                .and(maxX.greaterThanOrEqualTo(x))
                .and(minY.lessThanOrEqualTo(y))
                .and(maxY.greaterThanOrEqualTo(y));
    }


    public BooleanBinding contains(double x, double y, double z) {
        return minX.lessThanOrEqualTo(x)
                .and(maxX.greaterThanOrEqualTo(x))
                .and(minY.lessThanOrEqualTo(y))
                .and(maxY.greaterThanOrEqualTo(y))
                .and(minZ.lessThanOrEqualTo(z))
                .and(maxZ.greaterThanOrEqualTo(z));
    }


    public BooleanBinding contains(Bounds b) {
        return contains(b.getMinX(), b.getMinY(), b.getMinZ(), b.getWidth(), b.getHeight(), b.getDepth());
    }


    public BooleanBinding contains(double x, double y, double w, double h) {
        return minX.lessThanOrEqualTo(x)
                .and(maxX.greaterThanOrEqualTo(x + w))
                .and(minY.lessThanOrEqualTo(y))
                .and(maxY.greaterThanOrEqualTo(y + h));
    }


    public BooleanBinding contains(double x, double y, double z, double w, double h, double d) {
        return minX.lessThanOrEqualTo(x)
                .and(maxX.greaterThanOrEqualTo(x + w))
                .and(minY.lessThanOrEqualTo(y))
                .and(maxY.greaterThanOrEqualTo(y + h))
                .and(minZ.lessThanOrEqualTo(z))
                .and(maxZ.greaterThanOrEqualTo(z + d));
    }


    public BooleanBinding intersects(Bounds b) {
        return intersects(b.getMinX(), b.getMinY(), b.getMinZ(), b.getWidth(), b.getHeight(), b.getDepth());
    }


    public BooleanBinding intersects(double x, double y, double w, double h) {
        BooleanBinding bx = (minX.lessThan(x).and(minX.greaterThanOrEqualTo(x + w))).or(maxX.greaterThan(x).and(maxX.lessThanOrEqualTo(x + w)));
        BooleanBinding by = (minY.lessThan(y).and(minY.greaterThanOrEqualTo(y + h))).or(maxY.greaterThan(y).and(maxY.lessThanOrEqualTo(y + h)));
        return bx.and(by);
    }


    public BooleanBinding intersects(double x, double y, double z, double w, double h, double d) {
        BooleanBinding bx = (minX.lessThan(x).and(minX.greaterThanOrEqualTo(x + w))).or(maxX.greaterThan(x).and(maxX.lessThanOrEqualTo(x + w)));
        BooleanBinding by = (minY.lessThan(y).and(minY.greaterThanOrEqualTo(y + h))).or(maxY.greaterThan(y).and(maxY.lessThanOrEqualTo(y + h)));
        BooleanBinding bz = (minZ.lessThan(z).and(minZ.greaterThanOrEqualTo(z + d))).or(maxZ.greaterThan(z).and(maxZ.lessThanOrEqualTo(z + d)));
        return bx.and(by).and(bz);
    }


}
