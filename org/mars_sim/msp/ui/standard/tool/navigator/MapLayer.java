/**
 * Mars Simulation Project
 * MapLayer.java
 * @version 2.75 2003-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.Graphics;

/**
 * The MapLayer interface is a graphics layer painted on the map display.
 */
interface MapLayer {
    
    /**
     * Displays the layer on the map image.
     *
     * @param g graphics context of the map display.
     */
    public void displayLayer(Graphics g);
}
        
        
