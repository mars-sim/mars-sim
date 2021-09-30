package de.softknk.model.util;

import de.softknk.main.AppSettings;
import de.softknk.main.SoftknkioApp;

public interface MapPoint {

    static double randomX(double radius) {
        if (SoftknkioApp.matchfield == null) {
            return -((AppSettings.MAP_WIDTH - AppSettings.WINDOW_WIDTH) / 2) + Math.random() * (AppSettings.MAP_WIDTH - 2 * radius);
        } else {
            return SoftknkioApp.matchfield.getX() + Math.random() * (AppSettings.MAP_WIDTH - radius);
        }
    }

    static double randomY(double radius) {
        if (SoftknkioApp.matchfield == null) {
            return -((AppSettings.MAP_HEIGHT - AppSettings.WINDOW_HEIGHT) / 2) + Math.random() * (AppSettings.MAP_HEIGHT - radius * 2);
        } else {
            return SoftknkioApp.matchfield.getY() + Math.random() * (AppSettings.MAP_HEIGHT - radius);
        }
    }
}
