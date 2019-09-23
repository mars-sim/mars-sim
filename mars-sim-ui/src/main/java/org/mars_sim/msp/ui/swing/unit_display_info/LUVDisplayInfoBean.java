/**
 * Mars Simulation Project
 * LUVDisplayInfoFactory.java
 * @version 3.1.0 2017-10-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

import java.awt.Color;

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

		buttonIcon = ImageLoader.getIcon("LUVIcon", ImageLoader.TOOLBAR_ICON_DIR);
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
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}

    /**
     * Gets a sound appropriate for this unit.
     * @param unit the unit to display.
     * @return sound filepath for unit or empty string if none.
     */
	public String getSound(Unit unit) {
		LightUtilityVehicle luv = (LightUtilityVehicle) unit;
		StatusType status = luv.getStatus();
    	if (StatusType.MAINTENANCE.equals(status)) return SoundConstants.SND_ROVER_MAINTENANCE;
    	else if (StatusType.MALFUNCTION.equals(status)) return SoundConstants.SND_ROVER_MALFUNCTION;
    	else if (luv.getCrewNum() > 0 || luv.getRobotCrewNum() > 0) return SoundConstants.SND_ROVER_MOVING;
    	else return "";
	}


	@Override
	public Icon getGeologyMapIcon(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Color getGeologyMapLabelColor() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Color getGeologyGlobeColor() {
		// TODO Auto-generated method stub
		return null;
	}
}