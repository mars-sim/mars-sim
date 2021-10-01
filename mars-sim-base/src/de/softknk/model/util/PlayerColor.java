package de.softknk.model.util;

import javafx.scene.paint.Color;

public interface PlayerColor {

    Color DEFAULT = Color.rgb(0, 150, 230);

    static Color random(int color) {
        switch (color) {
            case 0:
                return DEFAULT;
            case 1:
                return Color.rgb(255, 0, 185);
            case 2:
                return Color.rgb(30, 185, 50);
            case 3:
                return Color.rgb(230, 175, 35);
            case 4:
                return Color.rgb(255, 0, 0);

            default:
                return Color.rgb(0, 0, 0);
        }
    }
}
