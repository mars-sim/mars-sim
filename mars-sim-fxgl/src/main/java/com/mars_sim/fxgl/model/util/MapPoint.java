package com.mars_sim.fxgl.model.util;

import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.fxgl.main.AppSettings;
import com.mars_sim.fxgl.main.SoftknkioApp;

public interface MapPoint {

    static double randomX(double radius) {
        if (SoftknkioApp.matchfield == null) {
            return -((AppSettings.MAP_WIDTH - AppSettings.WINDOW_WIDTH) / 2) + RandomUtil.getRandomDouble(AppSettings.MAP_WIDTH - 2 * radius);
        } else {
            return SoftknkioApp.matchfield.getX() + RandomUtil.getRandomDouble(AppSettings.MAP_WIDTH - radius);
        }
    }

    static double randomY(double radius) {
        if (SoftknkioApp.matchfield == null) {
            return -((AppSettings.MAP_HEIGHT - AppSettings.WINDOW_HEIGHT) / 2) + RandomUtil.getRandomDouble(AppSettings.MAP_HEIGHT - radius * 2);
        } else {
            return SoftknkioApp.matchfield.getY() + RandomUtil.getRandomDouble(AppSettings.MAP_HEIGHT - radius);
        }
    }
}
