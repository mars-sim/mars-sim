package de.softknk.model.operations;

import de.softknk.model.util.Loader;
import de.softknk.model.entities.Point;

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
