/**
 * Mars Simulation Project
 * UnitWindowListener.java
 * @version 2.75 2003-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import javax.swing.event.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;

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
        UnitWindow window = (UnitWindow) e.getSource();
        UnitUIProxy proxy = window.getProxy();
        desktop.disposeUnitWindow(proxy);
    }
}

