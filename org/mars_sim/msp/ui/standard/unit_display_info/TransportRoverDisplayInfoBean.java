/**
 * Mars Simulation Project
 * TransportRoverDisplayInfo.java
 * @version 2.75 2003-09-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_display_info;  
 
import javax.swing.*;
import org.mars_sim.msp.ui.standard.ImageLoader;

/**
 * Provides display information about a transport rover.
 */
class TransportRoverDisplayInfoBean extends VehicleDisplayInfoBean {
    
    // Data members
    private Icon buttonIcon;
    
    /**
     * Constructor
     */
    TransportRoverDisplayInfoBean() {
        super();
        buttonIcon = ImageLoader.getIcon("TransportRoverIcon");
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
