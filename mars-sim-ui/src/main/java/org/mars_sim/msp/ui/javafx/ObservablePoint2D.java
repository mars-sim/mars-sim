package org.mars_sim.msp.ui.javafx;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replacement to {@link javafx.geometry.Point2D} with observable {@link #x} and {@link #y} coords.
 * <p>
 *
 * @author dejv78 (dejv78.github.io)
 * @since 1.0.0
 */
public class ObservablePoint2D {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservablePoint2D.class);

    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();


    public ObservablePoint2D() {
        LOGGER.trace("ObservablePoint2D()");
    }


    public ObservablePoint2D(double x, double y) {
        LOGGER.trace("ObservablePoint2D(x={}, y={})", x, y);

        this.x.set(x);
        this.y.set(y);
    }


    public ObservablePoint2D(DoubleExpression x, DoubleExpression y) {
        LOGGER.trace("ObservablePoint2D(x={}, y={})", x, y);

        this.x.bind(x);
        this.y.bind(y);
    }


    public double getX() {
        return x.get();
    }


    public void setX(double x) {
        LOGGER.trace("setX({})", x);

        this.x.set(x);
    }


    public DoubleProperty xProperty() {
        return x;
    }


    public double getY() {
        return y.get();
    }


    public void setY(double y) {
        LOGGER.trace("setY({})", y);

        this.y.set(y);
    }


    public DoubleProperty yProperty() {
        return y;
    }


    @Override
    public String toString() {
        return "ObservablePoint2D [x: " + x + ", y: " + y + "]";
    }
}
