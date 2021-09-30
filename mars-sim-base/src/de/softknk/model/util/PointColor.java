package de.softknk.model.util;

import javafx.scene.paint.Color;

public interface PointColor {

    static javafx.scene.paint.Color random() {
        int random = (int) (Math.random() * 9);

        switch (random) {
            case 0:
                return Color.rgb(255, 0, 0);
            case 1:
                return Color.rgb(255, 255, 0);
            case 2:
                return Color.rgb(0, 255, 0);
            case 3:
                return Color.rgb(0, 70, 255);
            case 4:
                return Color.rgb(255, 0, 255);
            case 5:
                return Color.rgb(105, 0, 255);
            case 6:
                return Color.rgb(45, 135, 35);
            case 7:
                return Color.rgb(255, 140, 0);
            case 8:
                return Color.rgb(0, 255, 255);

            default:
                return Color.rgb(0, 0, 0);
        }
    }
}
