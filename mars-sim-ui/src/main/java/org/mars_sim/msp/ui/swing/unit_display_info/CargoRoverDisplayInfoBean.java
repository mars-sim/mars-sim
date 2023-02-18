/**
 * Mars Simulation Project
 * TransportRoverDisplayInfoBean.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.ui.swing.ImageLoader;

import javax.swing.Icon;

/**
 * Provides display information about a cargo rover.
 */
class CargoRoverDisplayInfoBean extends RoverDisplayInfoBean {
	
    // Data members
    private Icon buttonIcon = ImageLoader.getIconByName("vehicle/cargo");

    
    /**
     * Constructor
     */
    CargoRoverDisplayInfoBean() {
        super();
    }
    
    /** 
     * Gets icon for unit button.
     * @return icon
     */
    public Icon getButtonIcon() {
        return buttonIcon;
    }
}
