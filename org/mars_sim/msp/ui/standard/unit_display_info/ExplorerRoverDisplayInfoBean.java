/**
 * Mars Simulation Project
 * ExplorerRoverDisplayInfo.java
 * @version 2.75 2003-09-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_display_info;  
 
import javax.swing.*;
import org.mars_sim.msp.ui.standard.ImageLoader;

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
     *
     * @return icon
     */
    public Icon getButtonIcon() {
        return buttonIcon;
    }
}
