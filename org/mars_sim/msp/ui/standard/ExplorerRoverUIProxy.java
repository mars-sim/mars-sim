/**
 * Mars Simulation Project
 * ExplorerRoverUIProxy.java
 * @version 2.75 2003-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import javax.swing.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;

/**
 * User interface proxy for an explorer rover.
 */
public class ExplorerRoverUIProxy extends GroundVehicleUIProxy {

    private final static ImageIcon BUTTON_ICON =
                                    ImageLoader.getIcon("ExplorerRoverIcon");

    // Data members
    private ExplorerRover rover;

    /** Constructs an ExplorerRoverUIProxy object
     *  @param ExplorerRover the explorer rover
     *  @param proxyManager the unit UI proxy manager
     */
    public ExplorerRoverUIProxy(ExplorerRover rover,
            UIProxyManager proxyManager) {
        super(rover, proxyManager);

        this.rover = rover;
        buttonIcon = BUTTON_ICON;
    }
}
