/*
 * Mars Simulation Project
 * VehicleDisplayInfoBean.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.displayinfo;

import java.awt.Color;
import java.awt.Font;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Unit;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a vehicle.
 */
class VehicleDisplayInfoBean extends MapEntityDisplayInfo {
    private static final Font VEHICLE_MAP_FONT = new Font("Helvetica", Font.PLAIN, 10);
    /**
     * Constructor.
     */
    VehicleDisplayInfoBean(String buttonName) {
        super(buttonName, "vehicle", null, VEHICLE_MAP_FONT,
                Color.white, "map/vehicle", "map/vehicle_black");
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
        Vehicle vehicle = (Vehicle) unit;
        
        var container = vehicle.getContainerUnit();
		if (container == null || container instanceof MarsSurface)
        	return true;
        
        if (vehicle.isSalvaged()) result = false;
        
        // Do not display towed vehicle on map.
        if (vehicle.haveStatusType(StatusType.TOWED)) {
            result = false;
        }
        
        return result;
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

    /* (non-Javadoc)
	 * @see com.mars_sim.ui.standard.unit_display_info.UnitDisplayInfo#getSound(com.mars_sim.simulation.Unit)
	 */
	@Override
	public String getSound(Entity unit) {
		Vehicle rover = (Vehicle) unit;
		StatusType primStatus = rover.getPrimaryStatus();
    	if (primStatus == StatusType.MOVING) return SoundConstants.SND_ROVER_MOVING;
    	else if (rover.haveStatusType(StatusType.MAINTENANCE)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (rover.haveStatusType(StatusType.MALFUNCTION)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if ((primStatus == StatusType.GARAGED) || (primStatus == StatusType.PARKED)) return SoundConstants.SND_ROVER_PARKED;
    	else return "";
	}
}
