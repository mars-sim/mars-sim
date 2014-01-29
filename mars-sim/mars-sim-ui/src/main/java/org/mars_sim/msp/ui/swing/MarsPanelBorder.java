/**
 * Mars Simulation Project
 * MarsPanelBorder.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

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
