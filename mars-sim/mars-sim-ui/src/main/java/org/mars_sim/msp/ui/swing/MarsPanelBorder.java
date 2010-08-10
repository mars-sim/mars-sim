/**
 * Mars Simulation Project
 * MarsPanelBorder.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

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
