/**
 * Mars Simulation Project
 * TransportRoverDisplayInfoBean.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.ui.swing.ImageLoader;

import javax.swing.*;

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