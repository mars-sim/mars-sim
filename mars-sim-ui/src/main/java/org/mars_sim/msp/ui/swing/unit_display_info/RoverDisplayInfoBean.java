/**
 * Mars Simulation Project
 * RoverDisplayInfoBean.java
 * @version 3.1.0 2017-10-16
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

import javax.swing.*;

/**
 * Provides display information about a rover.
 */
public class RoverDisplayInfoBean extends VehicleDisplayInfoBean {

	// Data members
	private Icon buttonIcon;

	/**
	 * Constructor
	 */
	public RoverDisplayInfoBean() {
		super();
	}


	/**
	 * Gets the icon of this unit
	 * (non-Javadoc)
	 * @see org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfo#getButtonIcon()
	 * 
	 * @param unit
	 * @return the icon
	 */
	public Icon getButtonIcon(Unit unit) {
		Vehicle v = (Vehicle) unit;
		Rover rover = (Rover) v;
		String type = rover.getVehicleType().toLowerCase();
		
		if (type.contains("explorer"))
			buttonIcon = ImageLoader.getIcon("ExplorerRoverIcon", ImageLoader.TOOLBAR_ICON_DIR);
		else if (type.contains("cargo"))
			buttonIcon = ImageLoader.getIcon("CargoRoverIcon", ImageLoader.TOOLBAR_ICON_DIR);
		else if (type.contains("transport"))
			buttonIcon = ImageLoader.getIcon("TransportRoverIcon", ImageLoader.TOOLBAR_ICON_DIR);
		
		return buttonIcon;
	}

	/* (non-Javadoc)
	 * @see org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfo#getSound(org.mars_sim.msp.simulation.Unit)
	 */
	public String getSound(Unit unit) {
		Vehicle rover = (Vehicle) unit;
    	if (rover.haveStatusType(StatusType.MOVING)) return SoundConstants.SND_ROVER_MOVING;
    	else if (rover.haveStatusType(StatusType.MAINTENANCE)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (rover.haveStatusType(StatusType.MALFUNCTION)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if (rover.haveStatusType(StatusType.GARAGED) || rover.haveStatusType(StatusType.PARKED)) return SoundConstants.SND_ROVER_PARKED;
    	else return "";
	}
}