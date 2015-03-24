/**
 * Mars Simulation Project
 * LUVDisplayInfoFactory.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

import javax.swing.*;

/**
 * Provides display information about a light utility vehicle.
 */
public class LUVDisplayInfoBean extends VehicleDisplayInfoBean {

	// Data members
	private Icon buttonIcon;
	
	/**
	 * Constructor
	 */
	public LUVDisplayInfoBean() {
		// Use VehicleDisplayInfoBean
		super();
		
		buttonIcon = ImageLoader.getIcon("LUVIcon");
	}
	
	
	@Override
	public boolean isMapDisplayed(Unit unit) {
        return false;
    }
	
	@Override
    public boolean isGlobeDisplayed(Unit unit) {
        return false;
    }
	
    /** 
     * Gets icon for unit button.
     * @return icon
     */
	public Icon getButtonIcon() {
		return buttonIcon;
	}

    /**
     * Gets a sound appropriate for this unit.
     * @param unit the unit to display.
     * @return sound filepath for unit or empty string if none.
     */
	public String getSound(Unit unit) {
		LightUtilityVehicle luv = (LightUtilityVehicle) unit;
    	String status = luv.getStatus();
    	if (Vehicle.MAINTENANCE.equals(status)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (Vehicle.MALFUNCTION.equals(status)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if (luv.getCrewNum() > 0 || luv.getRobotCrewNum() > 0) return SoundConstants.SND_ROVER_MOVING;
    	else return "";
	}
}