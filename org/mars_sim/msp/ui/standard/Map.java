/**
 * Mars Simulation Project
 * Map.java
 * @version 2.71 2000-10-22
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.Coordinates;  
import java.awt.Image;

/** The Map interface represents a map usable by the MapDisplay class */
public interface Map {
    
    /** creates a 2D map at a given center point 
     *  @param newCenter the new center location
     */
    public void drawMap(Coordinates newCenter);
    
    /** determines if a requested map is complete 
     *  @return true if requested map is complete
     */
    public boolean isImageDone();
    
    /** returns constructed map image 
     *  @return constructed map image
     */
    public Image getMapImage();
}
