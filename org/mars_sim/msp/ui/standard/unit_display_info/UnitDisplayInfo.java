/**
 * Mars Simulation Project
 * UnitDisplayInfo.java
 * @version 2.75 2003-07-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_display_info;  
 
import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.Unit;

/**
 * Provides display information about a unit.
 */
public interface UnitDisplayInfo {
    
    /** 
     * Checks if unit is to be displayed on the navigator tool map.
     *
     * @param unit the unit to display
     * @return true if unit is to be displayed on navigator map.
     */
    public boolean isMapDisplayed(Unit unit);
    
    /** 
     * Gets display icon for the surface navigator map. 
     *
     * @return icon
     */
    public Icon getSurfMapIcon();
    
    /** 
     * Gets display icon for topo navigator map. 
     *
     * @return icon
     */
    public Icon getTopoMapIcon();
    
    /** 
     * Gets the label color for surface navigator map. 
     *
     * @return color
     */
    public Color getSurfMapLabelColor();
    
    /** 
     * Gets the label color for topo navigator map. 
     *
     * @return color
     */
    public Color getTopoMapLabelColor();
    
    /** 
     * Gets the label font for navigator map. 
     * 
     * @return font
     */
    public Font getMapLabelFont();

    /** 
     * Gets the range (km) for clicking on unit on navigator map. 
     *
     * @return clicking range
     */
    public double getMapClickRange();
    
    /** 
     * Checks if the unit is to be displayed on the navigator tool globe.
     *
     * @param unit the unit to display.
     * @return true if unit is to be displayed on globe
     */
    public boolean isGlobeDisplayed(Unit unit);
    
    /** 
     * Gets display color for surface globe. 
     *
     * @return color
     */
    public Color getSurfGlobeColor();
    
    /** 
     * Gets display color for topo globe.
     *
     * @return color
     */
    public Color getTopoGlobeColor();
    
    /** 
     * Gets icon for unit button.
     *
     * @return icon
     */
    public Icon getButtonIcon();
}
