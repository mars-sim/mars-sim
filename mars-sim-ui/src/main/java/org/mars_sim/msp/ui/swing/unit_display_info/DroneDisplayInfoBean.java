/**
 * Mars Simulation Project
 * DroneDisplayInfoBean.java
 * @version 3.1.2 2021-06-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import java.awt.Color;

import javax.swing.Icon;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a drone.
 */
public class DroneDisplayInfoBean extends VehicleDisplayInfoBean {

	// Data members
	private Icon buttonIcon = ImageLoader.getIcon(Msg.getString("ico.drone"), ImageLoader.VEHICLE_ICON_DIR);

	/**
	 * Constructor
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
    	else if (drone.haveStatusType(StatusType.GARAGED) || drone.haveStatusType(StatusType.PARKED)) return SoundConstants.SND_ROVER_PARKED;
    	else if (drone.getSpeed() > 0) return SoundConstants.SND_ROVER_MOVING;
    	else return "";
	}

}
