/**
 * Mars Simulation Project
 * Map.java
 * @version 2.75 2003-08-03
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator; 
 
import java.awt.Image;
import org.mars_sim.msp.simulation.Coordinates;  

/** 
 * The Map interface represents a map usable by the MapDisplay class.
 */
public interface Map {
    
    /** 
     * Creates a 2D map at a given center point.
     * 
     * @param newCenter the new center location
     */
    public void drawMap(Coordinates newCenter);
    
    /** 
     * Checks if a requested map is complete.
     * 
     * @return true if requested map is complete
     */
    public boolean isImageDone();
    
    /** 
     * Gets the constructed map image.
     * 
     * @return constructed map image
     */
    public Image getMapImage();
}
