/**
 * Mars Simulation Project
 * UnitWindowListener.java
 * @version 2.71 2000-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import javax.swing.event.*;

/** The UnitWindowListener class is a custom window listener for unit
 *  detail windows that handles their behavior.
 */
public class UnitWindowListener extends InternalFrameAdapter {

    // Data members
    MainDesktopPane desktop; // Main desktop pane that holds unit windows.

    /** Constructs a UnitWindowListener object
     *  @param desktop the desktop pane
     */
    public UnitWindowListener(MainDesktopPane desktop) {
        this.desktop = desktop;
    }

    // Overriden parent method
    /** Removes unit button from toolbar when unit window is closed. */
    public void internalFrameClosing(InternalFrameEvent e) {
        Unit unit = ((UnitDialog) e.getSource()).getUnit();
        UnitUIProxy proxy = desktop.getProxyManager().getUnitUIProxy(unit);
        desktop.disposeUnitWindow(proxy);
    }
}

