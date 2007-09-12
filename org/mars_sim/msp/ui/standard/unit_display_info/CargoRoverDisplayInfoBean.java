/**
 * Mars Simulation Project
 * TransportRoverDisplayInfoBean.java
 * @version 2.81 2007-09-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_display_info;

import javax.swing.Icon;

import org.mars_sim.msp.ui.standard.ImageLoader;

/**
 * Provides display information about a cargo rover.
 */
class CargoRoverDisplayInfoBean extends RoverDisplayInfoBean {
	
    // Data members
    private Icon buttonIcon;
    
    /**
     * Constructor
     */
    CargoRoverDisplayInfoBean() {
        super();
        buttonIcon = ImageLoader.getIcon("CargoRoverIcon");
    }
    
    /** 
     * Gets icon for unit button.
     * @return icon
     */
    public Icon getButtonIcon() {
        return buttonIcon;
    }
}