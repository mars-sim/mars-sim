/**
 * Mars Simulation Project
 * MarsPanelBorder.java
 * @version 2.75 2003-05-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import javax.swing.border.*;

/**
 * The MarsPanelBorder is a common compound border used for panels.
 */
public class MarsPanelBorder extends CompoundBorder {
    
    /**
     * Constructor
     */
    public MarsPanelBorder() {
        // Use CompoundBorder constructor
        super(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5));
    }
}
