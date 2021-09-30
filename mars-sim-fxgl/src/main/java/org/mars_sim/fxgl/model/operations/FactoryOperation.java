package org.mars_sim.fxgl.model.operations;

import org.mars_sim.fxgl.model.util.Loader;

public class FactoryOperation extends Operation {

    public static final String DEFAULT = "operations/factory.png";
    private int points_per_second;

    public FactoryOperation(int currentLevel) {
        super(Loader.loadImage(DEFAULT), currentLevel);
        doOperation();
    }

    @Override
    public void init() {
        //
    }

    @Override
    public int getPrice() {
        return (super.getLevel() + 1) * 32000;
    }

    @Override
    public void doOperation() {
        points_per_second = super.getLevel() * 200;
    }

    @Override
    public String operationName() {
        return "FACTORY";
    }

    public int getPointsPerSecond() {
        return points_per_second;
    }
}