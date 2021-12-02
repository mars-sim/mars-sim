package org.mars_sim.fxgl.model.operations;

import org.mars_sim.fxgl.model.util.Loader;

public class ExcavatorOperation extends Operation {

    public static final String DEFAULT = "operations/excavator.png";
    private int points_per_second;

    public ExcavatorOperation(int currentLevel) {
        super(Loader.loadImage(DEFAULT), currentLevel);
        doOperation();
    }

    @Override
    public void init() {
       //
    }

    @Override
    public int getPrice() {
        return (super.getLevel() + 1) * 8000;
    }

    @Override
    public void doOperation() {
        points_per_second = super.getLevel() * 50;
    }

    @Override
    public String operationName() {
        return "EXCAVATOR";
    }

    public int getPointsPerSecond() {
        return points_per_second;
    }
}
