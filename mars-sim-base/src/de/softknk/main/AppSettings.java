package de.softknk.main;

import java.awt.*;

public interface AppSettings {

    //SCREEN_RESOLUTION

    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

    //GAME SETTINGS

    String TITLE = "softknk.io";
    String VERSION = "3.5.1";
    String ICON = "favicon.png";

    int WINDOW_WIDTH = 1000; //(int) (gd.getDisplayMode().getWidth() * 0.55);
    int WINDOW_HEIGHT = 750; //(int) (gd.getDisplayMode().getHeight() * 0.7);

    int CELL_WIDTH = 125;
    int MAP_WIDTH = 2500;
    int MAP_HEIGHT = 2500;

    String DEFAULT_FONT = "BalooBhai.ttf";

    //PLAYER

    double PLAYER_RADIUS = (WINDOW_WIDTH * 0.16) / 2; //FULL-HD: 170.0
    double VELOCITY = 3.5;//WINDOW_WIDTH * 0.004; //FULL-HD: 3.3
}
