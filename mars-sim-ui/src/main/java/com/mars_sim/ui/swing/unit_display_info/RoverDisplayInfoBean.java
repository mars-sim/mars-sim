/*
 * Mars Simulation Project
 * RoverDisplayInfoBean.java
 * @date 2022-06-27
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_display_info;

import com.mars_sim.core.Unit;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a rover.
 */
abstract class RoverDisplayInfoBean extends VehicleDisplayInfoBean {

	// Data members
	//private Icon buttonIcon;

	/**
	 * Constructor.
	 */
	public RoverDisplayInfoBean() {
		super();
	}


	/* (non-Javadoc)
	 * @see com.mars_sim.ui.standard.unit_display_info.UnitDisplayInfo#getSound(com.mars_sim.simulation.Unit)
	 */
	@Override
	public String getSound(Unit unit) {
		Vehicle rover = (Vehicle) unit;
		StatusType primStatus = rover.getPrimaryStatus();
    	if (primStatus == StatusType.MOVING) return SoundConstants.SND_ROVER_MOVING;
    	else if (rover.haveStatusType(StatusType.MAINTENANCE)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (rover.haveStatusType(StatusType.MALFUNCTION)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if ((primStatus == StatusType.GARAGED) || (primStatus == StatusType.PARKED)) return SoundConstants.SND_ROVER_PARKED;
    	else return "";
	}
}
