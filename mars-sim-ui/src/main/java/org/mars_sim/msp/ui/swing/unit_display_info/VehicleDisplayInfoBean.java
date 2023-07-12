/*
 * Mars Simulation Project
 * VehicleDisplayInfoBean.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.mars.sim.mapdata.MapMetaData;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * Provides display information about a vehicle.
 */
abstract class VehicleDisplayInfoBean implements UnitDisplayInfo {
    
    // Navigator click range in km.
    private static double VEHICLE_CLICK_RANGE = 40D;
    
    // Data members
    private Icon blackMapIcon;
    private Icon normalMapIcon;
    private Font mapLabelFont;
    
    /**
     * Constructor.
     */
    VehicleDisplayInfoBean() {
        normalMapIcon = ImageLoader.getIconByName("map/vehicle");
        blackMapIcon = ImageLoader.getIconByName("map/vehicle_black");
        mapLabelFont = new Font("Helvetica", Font.PLAIN, 10);
    }
    
    /** 
     * Checks if unit is to be displayed on the navigator tool map.
     * 
     * @param unit the unit to display
     * @return true if unit is to be displayed on navigator map.
     */
    @Override
    public boolean isMapDisplayed(Unit unit) {
        boolean result = true;
        
        Unit container = unit.getContainerUnit();
		if (container == null || container instanceof MarsSurface)
        	result = true;
        
        Vehicle vehicle = (Vehicle) unit;
        if (vehicle.isSalvaged()) result = false;
        
        // Do not display towed vehicle on map.
        if (vehicle.haveStatusType(StatusType.TOWED)) {
            result = false;
        }
        
        return result;
    }
    
    /** 
     * Gets display icon for the navigator map.
     * 
     * @param unit the unit to display 
     * @param type Map details
     * @return icon
     */
    @Override
    public Icon getMapIcon(Unit unit, MapMetaData type) {
        return (type.isColourful() ? blackMapIcon : normalMapIcon);
    }
    
    /**
     * Checks if the map icon should blink on and off.
     * 
     * @param unit the unit to display
     * @return true if blink
     */
    @Override
    public boolean isMapBlink(Unit unit) {
    	return ((Vehicle) unit).isBeaconOn();
    }
    
    /** 
     * Gets the label color for surface navigator map.
     * 
     * @return color
     */
    @Override
    public Color getMapLabelColor(MapMetaData type) {
        return (type.isColourful() ? Color.black : Color.white);
    }
    
 
    /** 
     * Gets the label font for navigator map.
     *  
     * @return font
     */
    @Override
    public Font getMapLabelFont() {
        return mapLabelFont;
    }

    /** 
     * Gets the range (km) for clicking on unit on navigator map.
     *  
     * @return clicking range
     */
    @Override
    public double getMapClickRange() {
        return VEHICLE_CLICK_RANGE;
    }
    
    /** 
     * Checks if the unit is to be displayed on the navigator tool globe.
     * 
     * @param unit the unit to display.
     * @return true if unit is to be displayed on globe
     */
    @Override
    public boolean isGlobeDisplayed(Unit unit) {
        boolean result = true;
        
        Vehicle vehicle = (Vehicle) unit;
        
        // Show the vehicle only if it's on a mission outside
        int containerID = vehicle.getContainerID();
        result = (containerID == Unit.MARS_SURFACE_UNIT_ID || containerID == Unit.UNKNOWN_UNIT_ID)
                && (vehicle instanceof Rover || vehicle instanceof Drone);
		
        if (vehicle.isSalvaged()) 
        	result = false;
        
        return result;
    }
    
    /** 
     * Gets display color for surface globe.
     * 
     * @return color
     */
    @Override
    public Color getGlobeColor(MapMetaData type) {
        return (type.isColourful() ? Color.black : Color.white);
    }
    
    /** 
     * Gets icon for unit button.
     * To be overridden by sub-class.
     * 
     * @return icon
     */
    public Icon getButtonIcon() {
        return null;
    }
    
}
