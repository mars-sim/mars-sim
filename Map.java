/**
 * Mars Simulation Project
 * Map.java
 * @version 1.0 2000-09-03
 * @author Greg Whelan
 */

import java.awt.Image;

interface Map {
    public void drawMap(Coordinates newCenter);
    public boolean isImageDone();
    public Image getMapImage();
}
