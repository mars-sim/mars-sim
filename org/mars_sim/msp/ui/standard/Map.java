/**
 * Mars Simulation Project
 * Map.java
 * @version 2.71 2000-10-07
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.Coordinates;  
import java.awt.Image;

/** The Map interface represents a map usable by the MapDisplay class
 */
public interface Map {
    
    /** creates a 2D map at a given center point */
    public void drawMap(Coordinates newCenter);
    
    /** determines if a requested map is complete */
    public boolean isImageDone();
    
    /** returns constructed map image */
    public Image getMapImage();
}
