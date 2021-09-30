package de.softknk.model.entities;

import com.almasb.fxgl.entity.Entity;
import de.softknk.model.util.EntityType;
import de.softknk.model.util.Moveable;
import de.softknk.model.util.MapPoint;
import de.softknk.model.util.PointColor;
import javafx.scene.shape.Circle;

public class Point extends Entity implements Moveable {

    public double RADIUS;
    private static int scoreValue;

    public Point() {
        RADIUS = Math.random() * 5 + 3.2;
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
