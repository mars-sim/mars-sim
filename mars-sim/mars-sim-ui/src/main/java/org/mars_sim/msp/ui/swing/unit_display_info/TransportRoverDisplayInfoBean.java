/**
 * Mars Simulation Project
 * TransportRoverDisplayInfoBean.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;  
 
import javax.swing.*;

import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * Provides display information about a transport rover.
 */
class TransportRoverDisplayInfoBean extends RoverDisplayInfoBean {
    
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
     * @return icon
     */
    public Icon getButtonIcon() {
        return buttonIcon;
    }
}