/**
 * Mars Simulation Project
 * TransportRoverDisplayInfoBean.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.ImageLoader;

import javax.swing.*;

/**
 * Provides display information about a transport rover.
 */
class TransportRoverDisplayInfoBean extends RoverDisplayInfoBean {
    
    // Data members
    private Icon buttonIcon = ImageLoader.getIcon(Msg.getString("ico.transport"), ImageLoader.VEHICLE_ICON_DIR);
    
    /**
     * Constructor
     */
    TransportRoverDisplayInfoBean() {
        super();
//        buttonIcon = ImageLoader.getIcon("TransportRoverIcon", ImageLoader.VEHICLE_ICON_DIR);
    }
    
    /** 
     * Gets icon for unit button.
     * @return icon
     */
    public Icon getButtonIcon() {
        return buttonIcon;
    }
}
