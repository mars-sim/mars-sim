package com.mars_sim.fxgl.model.entities;

import com.almasb.fxgl.entity.Entity;
import com.mars_sim.fxgl.model.util.EntityType;
import com.mars_sim.fxgl.model.util.MapPoint;
import com.mars_sim.fxgl.model.util.Moveable;
import com.mars_sim.fxgl.model.util.PointColor;
import com.mars_sim.tools.util.RandomUtil;

import javafx.scene.shape.Circle;

public class Point extends Entity implements Moveable {

    public double RADIUS;
    private static int scoreValue;

    public Point() {
        RADIUS = RandomUtil.getRandomDouble(5.0) + 3.2;
        this.setType(EntityType.POINT);
        this.setPosition(MapPoint.randomX(RADIUS), MapPoint.randomY(RADIUS));
        this.getViewComponent().addChild(new Circle(RADIUS, PointColor.random()));
    }

    public static void initScoreValue(int level) {
        scoreValue = level;
    }

    public static void increaseScoreValue() {
        scoreValue += 1;
    }

    public static int scoreValue() {
        return scoreValue;
    }

    @Override
    public void moveX(double pixel) {
        this.translateX(pixel);
    }

    @Override
    public void moveY(double pixel) {
        this.translateY(pixel);
    }
}
