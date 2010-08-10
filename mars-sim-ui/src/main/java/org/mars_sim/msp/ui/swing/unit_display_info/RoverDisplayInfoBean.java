/**
 * Mars Simulation Project
 * RoverDisplayInfoBean.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_display_info;

import javax.swing.Icon;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

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
		buttonIcon = ImageLoader.getIcon("ExplorerRoverIcon");
	}

	/* (non-Javadoc)
	 * @see org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfo#getButtonIcon()
	 */
	public Icon getButtonIcon() {
		return buttonIcon;
	}

	/* (non-Javadoc)
	 * @see org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfo#getSound(org.mars_sim.msp.simulation.Unit)
	 */
	public String getSound(Unit unit) {
		Vehicle rover = (Vehicle) unit;
    	String status = rover.getStatus();
    	if (Vehicle.MOVING.equals(status)) return SoundConstants.SND_ROVER_MOVING;
    	else if (Vehicle.MAINTENANCE.equals(status)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (Vehicle.MALFUNCTION.equals(status)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if (Vehicle.PARKED.equals(status)) return SoundConstants.SND_ROVER_PARKED;
    	else return "";
	}
}