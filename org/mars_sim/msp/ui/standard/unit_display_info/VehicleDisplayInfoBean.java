/**
 * Mars Simulation Project
 * VehicleDisplayInfo.java
 * @version 2.75 2003-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_display_info;  
 
import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.ImageLoader;

/**
 * Provides display information about a vehicle.
 */
abstract class VehicleDisplayInfoBean implements UnitDisplayInfo {
    
    // Navigator click range in km.
    private static double VEHICLE_CLICK_RANGE = 40D;
    
    // Data members
    private Icon surfMapIcon;
    private Icon topoMapIcon;
    private Icon buttonIcon;
    private Font mapLabelFont;
    
    /**
     * Constructor
     */
    VehicleDisplayInfoBean() {
        surfMapIcon = ImageLoader.getIcon("VehicleSymbol");
        topoMapIcon = ImageLoader.getIcon("VehicleSymbolBlack");
        mapLabelFont = new Font("SansSerif", Font.PLAIN, 9);
    }
    
    /** 
     * Checks if unit is to be displayed on the navigator tool map.
     *
     * @param unit the unit to display
     * @return true if unit is to be displayed on navigator map.
     */
    public boolean isMapDisplayed(Unit unit) {
        Unit container = unit.getContainerUnit();
        if (container == null) return true;
        else return false;
    }
    
    /** 
     * Gets display icon for the surface navigator map. 
     *
     * @return icon
     */
    public Icon getSurfMapIcon() {
        return surfMapIcon;
    }
    
    /** 
     * Gets display icon for topo navigator map. 
     *
     * @return icon
     */
    public Icon getTopoMapIcon() {
        return topoMapIcon;
    }
    
    /** 
     * Gets the label color for surface navigator map. 
     *
     * @return color
     */
    public Color getSurfMapLabelColor() {
        return Color.white;
    }
    
    /** 
     * Gets the label color for topo navigator map. 
     *
     * @return color
     */
    public Color getTopoMapLabelColor() {
        return Color.black;
    }
    
    /** 
     * Gets the label font for navigator map. 
     * 
     * @return font
     */
    public Font getMapLabelFont() {
        return mapLabelFont;
    }

    /** 
     * Gets the range (km) for clicking on unit on navigator map. 
     *
     * @return clicking range
     */
    public double getMapClickRange() {
        return VEHICLE_CLICK_RANGE;
    }
    
    /** 
     * Checks if the unit is to be displayed on the navigator tool globe.
     *
     * @param unit the unit to display.
     * @return true if unit is to be displayed on globe
     */
    public boolean isGlobeDisplayed(Unit unit) {
        Unit container = unit.getContainerUnit();
        if (container == null) return true;
        else return false;
    }
    
    /** 
     * Gets display color for surface globe. 
     *
     * @return color
     */
    public Color getSurfGlobeColor() {
        return Color.white;
    }
    
    /** 
     * Gets display color for topo globe.
     *
     * @return color
     */
    public Color getTopoGlobeColor() {
        return Color.black;
    }
}
