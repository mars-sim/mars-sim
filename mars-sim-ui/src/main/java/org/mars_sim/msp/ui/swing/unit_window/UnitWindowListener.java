/**
 * Mars Simulation Project
 * UnitWindowListener.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import javax.swing.event.*;

import org.mars_sim.msp.ui.swing.*;

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

    /** 
     * Removes unit button from toolbar when unit window is closed. 
     *
     * @param e internal frame event.
     */
    public void internalFrameClosing(InternalFrameEvent e) {
        desktop.disposeUnitWindow((UnitWindow) e.getSource());
    }
}

