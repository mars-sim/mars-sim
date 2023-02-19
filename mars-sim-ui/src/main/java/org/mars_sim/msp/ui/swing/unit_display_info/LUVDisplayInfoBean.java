/**
 * Mars Simulation Project
 * LUVDisplayInfoFactory.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import java.awt.Color;

import javax.swing.Icon;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a light utility vehicle.
 */
public class LUVDisplayInfoBean extends VehicleDisplayInfoBean {

	// Data members
	private Icon buttonIcon = ImageLoader.getIconByName("unit/luv");


	/**
	 * Constructor
	 */
	public LUVDisplayInfoBean() {
		// Use VehicleDisplayInfoBean
		super();
	}

    /**
     * Gets icon for unit button.
     * 
     * @return icon
     */
	@Override
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}

    /**
     * Gets a sound appropriate for this unit.
     * @param unit the unit to display.
     * @return sound filepath for unit or empty string if none.
     */
	@Override
	public String getSound(Unit unit) {
		LightUtilityVehicle luv = (LightUtilityVehicle) unit;
    	if (luv.haveStatusType(StatusType.MAINTENANCE)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (luv.haveStatusType(StatusType.MALFUNCTION)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if ((luv.getPrimaryStatus() == StatusType.GARAGED) || (luv.getPrimaryStatus() == StatusType.PARKED)) return SoundConstants.SND_ROVER_PARKED;
    	else if (luv.getCrewNum() > 0 || luv.getRobotCrewNum() > 0) return SoundConstants.SND_ROVER_MOVING;
    	else return "";
	}


	@Override
	public Icon getGeologyMapIcon(Unit unit) {
		return null;
	}


	@Override
	public Color getGeologyMapLabelColor() {
		return null;
	}


	@Override
	public Color getGeologyGlobeColor() {
		return null;
	}
	
	@Override
	public boolean isMapDisplayed(Unit unit) {
        return false;
    }

	@Override
    public boolean isGlobeDisplayed(Unit unit) {
        return false;
    }
}
