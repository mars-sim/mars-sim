/**
 * Mars Simulation Project
 * Map.java
 * @version 2.75 2003-10-12
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map; 
 
import java.awt.Image;

import org.mars_sim.msp.core.Coordinates;

/** 
 * The Map interface represents a map usable by the MapDisplay class.
 */
public interface Map {
    
	public static final int DISPLAY_HEIGHT = 300; // Map display height in pixels.
	public static final int DISPLAY_WIDTH = 300; // Map display width in pixels.
	
    /** 
     * Creates a 2D map at a given center point.
     * 
     * @param newCenter the new center location
     * @throws Exception if error in drawing map.
     */
    public void drawMap(Coordinates newCenter) throws Exception;
    
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
