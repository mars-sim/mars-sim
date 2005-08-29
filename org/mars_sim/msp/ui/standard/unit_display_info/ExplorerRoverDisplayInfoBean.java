/**
 * Mars Simulation Project
 * ExplorerRoverDisplayInfo.java
 * @version 2.78 2005-08-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_display_info;  
 
import javax.swing.*;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.sound.SoundConstants;

/**
 * Provides display information about a explorer rover.
 */
class ExplorerRoverDisplayInfoBean extends VehicleDisplayInfoBean {
    
    // Data members
    private Icon buttonIcon;
    
    /**
     * Constructor
     */
    ExplorerRoverDisplayInfoBean() {
        super();
        buttonIcon = ImageLoader.getIcon("ExplorerRoverIcon");
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
     * @returns sound filepath for unit or empty string if none.
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