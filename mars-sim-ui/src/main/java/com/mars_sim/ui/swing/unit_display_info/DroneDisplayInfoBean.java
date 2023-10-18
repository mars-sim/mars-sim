/**
 * Mars Simulation Project
 * DroneDisplayInfoBean.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_display_info;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a drone.
 */
public class DroneDisplayInfoBean extends VehicleDisplayInfoBean {

	// Data members
	private Icon buttonIcon = ImageLoader.getIconByName("unit/drone");


	/**
	 * Constructor.
	 */
	public DroneDisplayInfoBean() {
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
		Drone drone = (Drone) unit;
    	if (drone.haveStatusType(StatusType.MAINTENANCE)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (drone.haveStatusType(StatusType.MALFUNCTION)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if ((drone.getPrimaryStatus() == StatusType.GARAGED) || (drone.getPrimaryStatus() == StatusType.PARKED)) return SoundConstants.SND_ROVER_PARKED;
    	else if (drone.getSpeed() > 0) return SoundConstants.SND_ROVER_MOVING;
    	else return "";
	}

}
