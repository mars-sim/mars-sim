package org.mars_sim.fxgl.model.operations;

import org.mars_sim.fxgl.model.util.Loader;
import org.mars_sim.fxgl.model.entities.Point;

public class PointOperation extends Operation {

    public static final String DEFAULT = "operations/point.png";

    public PointOperation(int currentLevel) {
        super(Loader.loadImage(DEFAULT), currentLevel);
    }

    @Override
    public void init() {
        //
    }

    @Override
    public int getPrice() {
        return super.getLevel() * 100;
    }

    @Override
    public void doOperation() {
        Point.increaseScoreValue();
    }

    @Override
    public String operationName() {
        return "POINT";
    }
}
