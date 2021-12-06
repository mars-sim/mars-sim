package org.mars_sim.fxgl.model.operations;

import org.mars_sim.fxgl.model.util.Loader;

public class MachineOperation extends Operation {

    public static final String DEFAULT = "operations/machine.png";
    private int points_per_second;

    public MachineOperation(int currentLevel) {
        super(Loader.loadImage(DEFAULT), currentLevel);
        doOperation();
    }

    @Override
    public void init() {
       //
    }

    @Override
    public int getPrice() {
        return (super.getLevel() + 1) * 4000;
    }

    @Override
    public void doOperation() {
        points_per_second = super.getLevel() * 10;
    }

    @Override
    public String operationName() {
        return "MACHINE";
    }

    public int getPointsPerSecond() {
        return points_per_second;
    }
}
