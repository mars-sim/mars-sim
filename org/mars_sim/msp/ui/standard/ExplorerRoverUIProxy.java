/**
 * Mars Simulation Project
 * ExplorerRoverUIProxy.java
 * @version 2.74 2002-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.awt.*;
import javax.swing.*;

/**
 * User interface proxy for an explorer rover.
 */
public class ExplorerRoverUIProxy extends GroundVehicleUIProxy {

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
        buttonIcon = new ImageIcon("images/ExplorerRoverIcon.gif");
    }

    /** Returns dialog window for explorer rover. 
     *  @return dialog window for explorer rover 
     */
    public UnitDialog getUnitDialog(MainDesktopPane desktop) {
        if (unitDialog == null) unitDialog = new ExplorerRoverDialog(desktop, this);
        return unitDialog;
    }
}
