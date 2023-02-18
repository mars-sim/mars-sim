/**
 * Mars Simulation Project
 * ExplorerRoverDisplayInfoBean.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.ui.swing.ImageLoader;

import javax.swing.Icon;

/**
 * Provides display information about a explorer rover.
 */
class ExplorerRoverDisplayInfoBean extends RoverDisplayInfoBean {
    
    // Data members
    private Icon buttonIcon = ImageLoader.getIconByName("vehicle/explorer");

    
    /**
     * Constructor
     */
    ExplorerRoverDisplayInfoBean() {
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
