/**
 * Mars Simulation Project
 * SurfaceMap.java
 * @version 1.0 2000-09-03
 * @author Greg Whelan
 */

import java.awt.Image;

interface SurfaceMap {
    public void drawMap(Coordinates newCenter);
    public boolean isImageDone();
    public Image getMapImage();
}
